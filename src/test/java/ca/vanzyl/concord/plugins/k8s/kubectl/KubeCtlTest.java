package ca.vanzyl.concord.plugins.k8s.kubectl;

import ca.vanzyl.concord.plugins.Configurator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.plugins.ConcordTestSupport;
import com.walmartlabs.concord.plugins.InterpolatingMockContext;
import ca.vanzyl.concord.plugins.k8s.kubectl.commands.Apply;
import ca.vanzyl.concord.plugins.k8s.kubectl.commands.Create;
import ca.vanzyl.concord.plugins.k8s.kubectl.commands.Delete;
import ca.vanzyl.concord.plugins.tool.*;
import com.walmartlabs.concord.plugins.OKHttpDownloadManager;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.MockContext;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class KubeCtlTest extends ConcordTestSupport
{

    private Configurator toolConfigurator;

    @Before
    public void setUp() throws Exception {
        toolConfigurator = new Configurator();
        super.setUp();
    }

    @Test
    public void validateToolInitializerWithKubeCtl() throws Exception {

        Path workingDirectory = Files.createTempDirectory("concord");
        deleteDirectory(workingDirectory);

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        ToolDescriptor toolDescriptor = ToolTaskSupport.fromResource("kubectl");
        ToolInitializationResult result = toolInitializer.initialize(workingDirectory, toolDescriptor);

        assertThat(result.executable().toFile().exists()).isTrue();
        assertThat(Files.isExecutable(result.executable())).isTrue();

        deleteDirectory(workingDirectory);
    }

    @Test
    public void validateConfiguratorUsingNameAndVersion() throws Exception {

        Map<String, Object> input = mapBuilder()
                .put("command", "apply")
                .put("file", "00-helm/tiller-rbac.yml")
                .build();

        Apply apply = new Apply();
        toolConfigurator.configure(apply, input);

        assertThat(apply.file()).isEqualTo("00-helm/tiller-rbac.yml");
    }

    @Test
    public void validateKubeCtlApply() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("kubectl/apply", new Apply());
        KubeCtlTask task = new KubeCtlTask(commands, lockService, toolInitializer);

        // Create a manifest.yml file and make sure it's interpolated correctly
        Path manifestYamlFile = Files.createTempFile("kubectl", "values.yaml");
        System.out.println("Creating K8s manifest.yml: " + manifestYamlFile.toString());

        String manifestContent = "apiVersion: v1\n" +
                "kind: Service\n" +
                "metadata:\n" +
                "  name: my-service\n" +
                "spec:\n" +
                "  selector:\n" +
                "    app: MyApp\n" +
                "  ports:\n" +
                "    - protocol: TCP\n" +
                "      port: 80\n" +
                "      targetPort: ${targetPort}";

        Files.write(manifestYamlFile, manifestContent.getBytes());

        Map<String, Object> args = Maps.newHashMap(mapBuilder()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "apply")
                .put("file", manifestYamlFile.toString())
                .put("envars",
                        mapBuilder()
                                .put("KUBECONFIG", "/workspace/_attachments/k8s-cluster0-kubeconfig")
                                .build())
                .put("targetPort", "123456789")
                .build());

        Context context = new InterpolatingMockContext(args);
        task.execute(context);

        // Make sure our values.yaml file was interpolated correctly by the Helm install command
        String interpolatedContent = new String(Files.readAllBytes(manifestYamlFile));
        assertThat(interpolatedContent).contains("targetPort: 123456789");

        String expectedCommandLine = String.format("kubectl apply -f %s", manifestYamlFile.toString());
        assertThat(normalizedCommandLineArguments(context)).isEqualTo(expectedCommandLine);

        System.out.println(context.getVariable("envars"));

        // If these were placed in the context, then they were added to the environment of the executed command
        assertEquals("/workspace/_attachments/k8s-cluster0-kubeconfig", varAsMap(context, "envars").get("KUBECONFIG"));
    }

    @Test
    public void validateKubeCtlApplyWithValidateFalse() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("kubectl/apply", new Apply());
        KubeCtlTask task = new KubeCtlTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(mapBuilder()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "apply")
                .put("validate", "false")
                .put("file", "00-helm/tiller-rbac.yml")
                .put("envars",
                        mapBuilder()
                                .put("KUBECONFIG", "/workspace/_attachments/k8s-cluster0-kubeconfig")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);

        String expectedCommandLine = "kubectl apply --validate=false -f 00-helm/tiller-rbac.yml";
        assertThat(normalizedCommandLineArguments(context)).isEqualTo(expectedCommandLine);

        System.out.println(context.getVariable("envars"));

        // If these were placed in the context, then they were added to the environment of the executed command
        assertEquals("/workspace/_attachments/k8s-cluster0-kubeconfig", varAsMap(context, "envars").get("KUBECONFIG"));
    }

    @Test
    public void validateKubeCtlCreateNamespace() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("kubectl/create", new Create());
        KubeCtlTask task = new KubeCtlTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(mapBuilder()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "create")
                .put("namespace",
                        mapBuilder()
                                .put("name", "cert-manager")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);

        String expectedCommandLine = "kubectl create namespace cert-manager";
        assertThat(normalizedCommandLineArguments(context)).isEqualTo(expectedCommandLine);
    }

    @Test
    public void validateKubeCtlDelete() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("kubectl/delete", new Delete());
        KubeCtlTask task = new KubeCtlTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(mapBuilder()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "delete")
                .put("crd", "alertmanagers.monitoring.coreos.com")
                .build());

        Context context = new MockContext(args);
        task.execute(context);

        String expectedCommandLine = "kubectl delete crd alertmanagers.monitoring.coreos.com";
        assertThat(normalizedCommandLineArguments(context)).isEqualTo(expectedCommandLine);
    }
}
