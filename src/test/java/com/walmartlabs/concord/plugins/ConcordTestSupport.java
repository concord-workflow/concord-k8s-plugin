package com.walmartlabs.concord.plugins;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.walmartlabs.concord.common.IOUtils;
import com.walmartlabs.concord.plugins.tool.ToolInitializerTest;
import com.walmartlabs.concord.sdk.*;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ConcordTestSupport {

    protected String basedir;
    protected final static String CONCORD_TMP_DIR_KEY = "CONCORD_TMP_DIR";
    protected final static String CONCORD_TMP_DIR_VALUE = "/tmp/concord";
    protected final static String CONCORD_AWS_CREDENTIALS_KEY = "concord-integration-tests";
    protected AWSCredentials awsCredentials;
    protected Path workDir;
    protected LockService lockService;
    protected ObjectStorage objectStorage;
    protected SecretService secretService;
    protected DependencyManager dependencyManager;

    @Before
    public void setUp() throws Exception {
        basedir = new File("").getAbsolutePath();

        workDir = workDir();

        RequiresAwsCredentials requiresAws = getClass().getAnnotation(RequiresAwsCredentials.class);
        if (requiresAws != null) {
            awsCredentials = awsCredentials();
            System.out.println("Using the following AWS credentials:");
            System.out.println();
            if (awsCredentials != null) {
                System.out.println("AWS Access Key ID: " + awsCredentials.accessKey);
                System.out.println("AWS Secret Key   : " + awsCredentials.secretKey);
            }
            System.out.println("workDir: " + workDir);
            System.out.println();
        }

        Files.createDirectories(workDir);

        lockService = mock(LockService.class);
        secretService = createSecretService(workDir);
        dependencyManager = new ToolInitializerTest.OKHttpDownloadManager("terraform");
    }

    protected File target(String name) {

        File target = new File(basedir, "target/" + name);
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        return target;
    }

    public static ImmutableMap.Builder<String, Object> mapBuilder() {
        return ImmutableMap.builder();
    }

    //
    // Context/Variable utils
    //
    protected String varAsString(Context context, String name) {
        return ((String) context.getVariable(name));
    }

    protected Map<String, String> varAsMap(Context context, String name) {
        return ((Map<String, String>) context.getVariable(name));
    }

    //
    // File/Directory utils
    //
    protected void deleteDirectory(Path pathToBeDeleted) throws IOException {
        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    // TODO: move to shared helper class
    public static ObjectStorage createObjectStorage(WireMockRule wireMockRule) throws Exception {
        String osAddress = "http://localhost:" + wireMockRule.port() + "/test";
        wireMockRule.stubFor(get("/test").willReturn(aResponse().withStatus(404)));
        wireMockRule.stubFor(post("/test").willReturn(aResponse().withStatus(200)));

        ObjectStorage os = mock(ObjectStorage.class);
        when(os.createBucket(any(), anyString())).thenReturn(ImmutableBucketInfo.builder()
                .address(osAddress)
                .build());

        return os;
    }

    private static SecretService createSecretService(Path workDir) throws Exception {
        String pemFileEnvar = System.getenv("PRIVATE_KEY_PATH");
        Path dst = Files.createTempFile(workDir, "private", ".key");
        if (pemFileEnvar != null) {
            Files.copy(Paths.get(pemFileEnvar), dst, StandardCopyOption.REPLACE_EXISTING);
        } else {
            // Look for an ~/.aws/concord-integration-tests.pem file
            File pemFile = new File(System.getProperty("user.hom"), ".aws/" + CONCORD_AWS_CREDENTIALS_KEY + ".pem");
            if (pemFile.exists()) {
                Files.copy(pemFile.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Map<String, String> m = Collections.singletonMap("private", workDir.relativize(dst).toString());

        SecretService ss = mock(SecretService.class);
        when(ss.exportKeyAsFile(any(), any(), any(), any(), any(), any())).thenReturn(m);

        return ss;
    }

    private static Map<String, Properties> parseIni(File file) {
        try (Reader reader = new FileReader(file)) {
            Map<String, Properties> result = new HashMap();
            new Properties() {

                private Properties section;

                @Override
                public Object put(Object key, Object value) {
                    String header = (key + " " + value).trim();
                    if (header.startsWith("[") && header.endsWith("]")) {
                        return result.put(header.substring(1, header.length() - 1), section = new Properties());
                    } else {
                        return section.put(key, value);
                    }
                }

            }.load(reader);
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    ///

    private static void setNewEnvironment(Map<String, String> newEnvironment) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newEnvironment);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newEnvironment);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newEnvironment);
                }
            }
        }
    }

    //
    // Helpers for using AWS credentials in ~/.aws/credentials
    //

    private AWSCredentials awsCredentials() {
        AWSCredentials awsCredentials = new AWSCredentials();
        File awsCredentialsFile = new File(System.getProperty("user.home"), ".aws/credentials");
        if (!awsCredentialsFile.exists()) {
            return null;
        }

        Map<String, Properties> awsCredentialsIni = parseIni(awsCredentialsFile);
        if (awsCredentialsIni != null) {
            Properties concordAwsCredentials = awsCredentialsIni.get(CONCORD_AWS_CREDENTIALS_KEY);
            if (concordAwsCredentials == null) {
                throw new RuntimeException("You must have a [" + CONCORD_AWS_CREDENTIALS_KEY + "] stanza in your ~/.aws/credentials !");
            }
            awsCredentials.accessKey = concordAwsCredentials.getProperty("aws_access_key_id");
            awsCredentials.secretKey = concordAwsCredentials.getProperty("aws_secret_access_key");
        }

        if (awsCredentials.accessKey.isEmpty() && awsCredentials.secretKey.isEmpty()) {
            awsCredentials.accessKey = System.getenv("AWS_ACCESS_KEY");
            if (awsCredentials.accessKey == null) {
                awsCredentials.accessKey = System.getenv("AWS_ACCESS_KEY_ID");
            }
            awsCredentials.secretKey = System.getenv("AWS_SECRET_KEY");
            if (awsCredentials.secretKey == null) {
                awsCredentials.secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
            }
        }

        if (awsCredentials.accessKey.isEmpty() && awsCredentials.secretKey.isEmpty()) {
            throw new RuntimeException(String.format("An AWS access key id and secret key must be set using envars or the ~/.aws/credentials file with the %s profile.", CONCORD_AWS_CREDENTIALS_KEY));
        }

        return awsCredentials;
    }

    private Path workDir() throws Exception {
        String concordTmpDir = System.getenv(CONCORD_TMP_DIR_KEY);
        if (concordTmpDir == null) {
            // Grab the old environment and add the CONCORD_TMP_DIR value to it and reset it
            Map<String, String> newEnvironment = new HashMap();
            newEnvironment.putAll(System.getenv());
            newEnvironment.put(CONCORD_TMP_DIR_KEY, CONCORD_TMP_DIR_VALUE);
            setNewEnvironment(newEnvironment);
        }
        return IOUtils.createTempDir("test");
    }

    //
    // Helpers for setting envars and setting CONCORD_TMP_DIR envar
    //

    public static class OKHttpDownloadManager implements DependencyManager {

        private final File toolDir;

        public OKHttpDownloadManager(String tool) {
            this.toolDir = new File(System.getProperty("user.home"), ".m2/tools/" + tool);
            this.toolDir.mkdirs();
        }

        @Override
        public Path resolve(URI uri) throws IOException {
            String urlString = uri.toString();
            String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
            File target = new File(toolDir, fileName);
            if (!target.exists()) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(urlString).build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.code() == 404) {
                    throw new RuntimeException(String.format("The URL %s doesn't exist.", uri));
                }
                download(response.body().byteStream(), target);
            }
            return target.toPath();
        }

        //
        // https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
        //
        // surprised this is the fastest way to convert an inputstream to a string
        //
        private void download(InputStream stream, File target) throws IOException {
            byte[] buffer = new byte[8192];
            int length;
            try (OutputStream result = new FileOutputStream(target)) {
                while ((length = stream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
            }
        }
    }

    private static class AWSCredentials {

        String accessKey;
        String secretKey;

        @Override
        public String toString() {
            return "AWSCredentials{" +
                    "accessKey='" + accessKey + '\'' +
                    ", secretKey='" + secretKey + '\'' +
                    '}';
        }
    }
}
