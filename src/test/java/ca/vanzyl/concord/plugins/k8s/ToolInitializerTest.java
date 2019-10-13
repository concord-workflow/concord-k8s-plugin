package ca.vanzyl.concord.plugins.k8s;

import static org.junit.Assert.assertTrue;

import ca.vanzyl.concord.plugins.k8s.ToolDescriptor.NamingStyle;
import ca.vanzyl.concord.plugins.k8s.ToolDescriptor.Packaging;
import com.walmartlabs.concord.sdk.DependencyManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

public class ToolInitializerTest {

  @Test
  public void validateToolInitializerWithEksCtl() throws Exception {

    Path workingDirectory = Files.createTempDirectory("concord");
    deleteDirectory(workingDirectory);

    ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("eksctl"));

    ToolDescriptor toolDescriptor = ImmutableToolDescriptor.builder()
        .id("eksctl")
        .name("EksCtl")
        .executable("eksctl")
        .architecture("amd64")
        .namingStyle(NamingStyle.CAPITALIZE)
        .packaging(Packaging.TARGZ)
        .defaultVersion(EksCtlTask.DEFAULT_VERSION)
        .urlTemplate(EksCtlTask.URL_TEMPLATE)
        .build();

    ToolInitializationResult result = toolInitializer.initialize(workingDirectory, toolDescriptor);

    assertTrue(result.executable().toFile().exists());
    assertTrue(Files.isExecutable(result.executable()));

    deleteDirectory(workingDirectory);
  }

  @Test
  public void validateToolInitializerWithHelm() throws Exception {

    Path workingDirectory = Files.createTempDirectory("concord");
    deleteDirectory(workingDirectory);

    ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("helm"));

    ToolDescriptor toolDescriptor = ImmutableToolDescriptor.builder()
        .id("helm")
        .name("Helm")
        .executable("helm")
        .architecture("amd64")
        .namingStyle(NamingStyle.LOWER)
        .packaging(Packaging.TARGZ_STRIP)
        .defaultVersion(HelmTask.DEFAULT_VERSION)
        .urlTemplate(HelmTask.URL_TEMPLATE)
        .build();

    ToolInitializationResult result = toolInitializer.initialize(workingDirectory, toolDescriptor);

    assertTrue(result.executable().toFile().exists());
    assertTrue(Files.isExecutable(result.executable()));

    deleteDirectory(workingDirectory);
  }

  @Test
  public void validateToolInitializerWithKubeCtl() throws Exception {

    //Path workingDirectory = Files.createTempDirectory("concord");
    Path workingDirectory = Paths.get("/tmp/junk");
    deleteDirectory(workingDirectory);

    ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));

    ToolDescriptor toolDescriptor = ImmutableToolDescriptor.builder()
        .id("kubectl")
        .name("KubeCtl")
        .executable("kubectl")
        .architecture("amd64")
        .namingStyle(NamingStyle.LOWER)
        .packaging(Packaging.FILE)
        .defaultVersion(KubeCtlTask.DEFAULT_VERSION)
        .urlTemplate(KubeCtlTask.URL_TEMPLATE)
        .build();

    ToolInitializationResult result = toolInitializer.initialize(workingDirectory, toolDescriptor);

    assertTrue(result.executable().toFile().exists());
    assertTrue(Files.isExecutable(result.executable()));

    //deleteDirectory(workingDirectory);
  }

  static class OKHttpDownloadManager implements DependencyManager {

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

  protected void deleteDirectory(Path pathToBeDeleted) throws IOException {
    Files.walk(pathToBeDeleted)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }
}
