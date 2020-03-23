package com.walmartlabs.concord.plugins.k8s.kubectl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.plugins.ConcordTestSupport;
import com.walmartlabs.concord.plugins.Configurator;
import com.walmartlabs.concord.plugins.InterpolatingMockContext;
import com.walmartlabs.concord.plugins.k8s.kubectl.commands.Apply;
import com.walmartlabs.concord.plugins.k8s.kubectl.commands.Create;
import com.walmartlabs.concord.plugins.k8s.kubectl.commands.Delete;
import com.walmartlabs.concord.plugins.tool.*;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.MockContext;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        assertTrue(result.executable().toFile().exists());
        assertTrue(Files.isExecutable(result.executable()));

        deleteDirectory(workingDirectory);
    }

    @Test
    public void validateConfiguratorUsingNameAndVersion() throws Exception {

        Map<String, Object> input = taskVariables()
                .put("command", "apply")
                .put("file", "00-helm/tiller-rbac.yml")
                .build();

        Apply apply = new Apply();
        toolConfigurator.configure(apply, input);

        assertEquals("00-helm/tiller-rbac.yml", apply.file());
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

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "apply")
                .put("file", manifestYamlFile.toString())
                .put("envars",
                        taskVariables()
                                .put("KUBECONFIG", "/workspace/_attachments/k8s-cluster0-kubeconfig")
                                .build())
                .put("targetPort", "123456789")
                .build());

        Context context = new InterpolatingMockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        // Make sure our values.yaml file was interpolated correctly by the Helm install command
        String interpolatedContent = new String(Files.readAllBytes(manifestYamlFile));
        assertTrue(interpolatedContent.contains("targetPort: 123456789"));

        String expectedCommandLine = String.format("kubectl apply -f %s", manifestYamlFile.toString());
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));

        System.out.println(context.getVariable("envars"));

        // If these were placed in the context, then they were added to the environment of the executed command
        assertEquals("/workspace/_attachments/k8s-cluster0-kubeconfig", varAsMap(context, "envars").get("KUBECONFIG"));
    }

    @Test
    public void validateKubeCtlApplyWithValidateFalse() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("kubectl/apply", new Apply());
        KubeCtlTask task = new KubeCtlTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "apply")
                .put("validate", "false")
                .put("file", "00-helm/tiller-rbac.yml")
                .put("envars",
                        taskVariables()
                                .put("KUBECONFIG", "/workspace/_attachments/k8s-cluster0-kubeconfig")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        String expectedCommandLine = "kubectl apply --validate=false -f 00-helm/tiller-rbac.yml";
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));

        System.out.println(context.getVariable("envars"));

        // If these were placed in the context, then they were added to the environment of the executed command
        assertEquals("/workspace/_attachments/k8s-cluster0-kubeconfig", varAsMap(context, "envars").get("KUBECONFIG"));
    }

    @Test
    public void validateKubeCtlCreateNamespace() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("kubectl/create", new Create());
        KubeCtlTask task = new KubeCtlTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "create")
                .put("namespace",
                        taskVariables()
                                .put("name", "cert-manager")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        String expectedCommandLine = "kubectl create namespace cert-manager";
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));
    }

    @Test
    public void validateKubeCtlDelete() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("kubectl/delete", new Delete());
        KubeCtlTask task = new KubeCtlTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "delete")
                .put("crd", "alertmanagers.monitoring.coreos.com")
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        String expectedCommandLine = "kubectl delete crd alertmanagers.monitoring.coreos.com";
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));
    }
}
