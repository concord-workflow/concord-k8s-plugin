package ca.vanzyl.concord.plugins.k8s.helm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.plugins.ConcordTestSupport;
import ca.vanzyl.concord.plugins.Configurator;
import com.walmartlabs.concord.plugins.InterpolatingMockContext;
import ca.vanzyl.concord.plugins.k8s.helm.commands.*;
import ca.vanzyl.concord.plugins.tool.*;
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

public class HelmTest extends ConcordTestSupport
{

    private Configurator toolConfigurator;

    @Before
    public void setUp() throws Exception {
        toolConfigurator = new Configurator();
        super.setUp();
    }

    @Test
    public void validateToolInitializerWithHelm() throws Exception {

        Path workingDirectory = Files.createTempDirectory("concord");
        deleteDirectory(workingDirectory);

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("helm"));
        ToolDescriptor toolDescriptor = ToolTaskSupport.fromResource("helm");
        ToolInitializationResult result = toolInitializer.initialize(workingDirectory, toolDescriptor);

        assertTrue(result.executable().toFile().exists());
        assertTrue(Files.isExecutable(result.executable()));

        deleteDirectory(workingDirectory);
    }

    @Test
    public void validateConfiguratorUsingNameAndVersion() throws Exception {

        Map<String, Object> input = taskVariables()
                .put("command", "init")
                .put("serviceAccount", "tiller")
                .put("wait", "true")
                .build();

        Init init = new Init();
        toolConfigurator.configure(init, input);

        assertEquals("tiller", init.serviceAccount());
        assertTrue("tiller", init.waitForCompletion());
    }

    @Test
    public void validateHelmInit() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("helm"));
        Map<String, ToolCommand> commands = ImmutableMap.of("helm/init", new Init());
        HelmTask task = new HelmTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "init")
                .put("serviceAccount", "tiller")
                .put("wait", "true")
                .put("envars",
                        taskVariables()
                                .put("AWS_ACCESS_KEY_ID", "aws-access-key")
                                .put("AWS_SECRET_ACCESS_KEY", "aws-secret-key")
                                .put("KUBECONFIG", "/workspace/_attachments/k8s-cluster0-kubeconfig")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        String expectedCommandLine = "helm init --service-account tiller --wait";
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));

        System.out.println(context.getVariable("envars"));

        // If these were placed in the context, then they were added to the environment of the executed command
        assertEquals("/workspace/_attachments/k8s-cluster0-kubeconfig", varAsMap(context, "envars").get("KUBECONFIG"));
        assertEquals("aws-access-key", varAsMap(context, "envars").get("AWS_ACCESS_KEY_ID"));
        assertEquals("aws-secret-key", varAsMap(context, "envars").get("AWS_SECRET_ACCESS_KEY"));
    }

    @Test
    public void validateHelmInstall() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("helm"));
        Map<String, ToolCommand> commands = ImmutableMap.of("helm/install", new Install());
        HelmTask task = new HelmTask(commands, lockService, toolInitializer);

        // Create a values.yml file and make sure it's interpolated correctly
        Path valuesYaml = Files.createTempFile("helm", "values.yaml");
        System.out.println("Creating Helm values.yml: " + valuesYaml.toString());
        StringBuffer sb = new StringBuffer();
        sb.append("ingress:").append("\n");
        sb.append("  hostname: ${hostname}");
        Files.write(valuesYaml, sb.toString().getBytes());

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "install")
                .put("chart",
                        taskVariables()
                                .put("name", "sealed-secrets")
                                .put("namespace", "kube-system")
                                .put("version", "1.4.2")
                                .put("set", ImmutableList.of("expose.ingress.host.core=bob.fetesting.com"))
                                .put("value", "stable/sealed-secrets")
                                .put("values", valuesYaml.toString())
                                .build())
                .put("envars",
                        taskVariables()
                                .put("AWS_ACCESS_KEY_ID", "aws-access-key")
                                .put("AWS_SECRET_ACCESS_KEY", "aws-secret-key")
                                .put("KUBECONFIG", "/workspace/_attachments/k8s-cluster0-kubeconfig")
                                .build())
                .put("hostname", "awesome.concord.io")
                .build());

        Context context = new InterpolatingMockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        // Make sure our values.yaml file was interpolated correctly by the Helm install command
        String interpolatedContent = new String(Files.readAllBytes(valuesYaml));
        assertTrue(interpolatedContent.contains("hostname: awesome.concord.io"));

        String expectedCommandLine = String.format("helm install --atomic --namespace kube-system --version 1.4.2 --set expose.ingress.host.core=bob.fetesting.com --values %s --name sealed-secrets stable/sealed-secrets", valuesYaml.toString());
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));

        System.out.println(context.getVariable("envars"));

        // If these were placed in the context, then they were added to the environment of the executed command
        assertEquals("/workspace/_attachments/k8s-cluster0-kubeconfig", varAsMap(context, "envars").get("KUBECONFIG"));
        assertEquals("aws-access-key", varAsMap(context, "envars").get("AWS_ACCESS_KEY_ID"));
        assertEquals("aws-secret-key", varAsMap(context, "envars").get("AWS_SECRET_ACCESS_KEY"));
    }

    @Test
    public void validateHelmUpgrade() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("helm"));
        Map<String, ToolCommand> commands = ImmutableMap.of("helm/upgrade", new Upgrade());
        HelmTask task = new HelmTask(commands, lockService, toolInitializer);

        // Create a values.yml file and make sure it's interpolated correctly
        Path valuesYaml = Files.createTempFile("helm", "values.yaml");
        System.out.println("Creating Helm values.yml: " + valuesYaml.toString());
        StringBuffer sb = new StringBuffer();
        sb.append("ingress:").append("\n");
        sb.append("  hostname: ${hostname}");
        Files.write(valuesYaml, sb.toString().getBytes());

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "upgrade")
                .put("chart",
                        taskVariables()
                                .put("name", "sealed-secrets")
                                .put("namespace", "kube-system")
                                .put("version", "1.4.2")
                                .put("set", ImmutableList.of("expose.ingress.host.core=bob.fetesting.com"))
                                .put("value", "stable/sealed-secrets")
                                .put("values", valuesYaml.toString())
                                .build())
                .put("envars",
                        taskVariables()
                                .put("AWS_ACCESS_KEY_ID", "aws-access-key")
                                .put("AWS_SECRET_ACCESS_KEY", "aws-secret-key")
                                .put("KUBECONFIG", "/workspace/_attachments/k8s-cluster0-kubeconfig")
                                .build())
                .put("hostname", "awesome.concord.io")
                .build());

        Context context = new InterpolatingMockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        // Make sure our values.yaml file was interpolated correctly by the Helm install command
        String interpolatedContent = new String(Files.readAllBytes(valuesYaml));
        assertTrue(interpolatedContent.contains("hostname: awesome.concord.io"));

        String expectedCommandLine = String.format("helm upgrade --install --atomic --namespace kube-system --version 1.4.2 --set expose.ingress.host.core=bob.fetesting.com --values %s sealed-secrets stable/sealed-secrets", valuesYaml.toString());
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));

        System.out.println(context.getVariable("envars"));

        // If these were placed in the context, then they were added to the environment of the executed command
        assertEquals("/workspace/_attachments/k8s-cluster0-kubeconfig", varAsMap(context, "envars").get("KUBECONFIG"));
        assertEquals("aws-access-key", varAsMap(context, "envars").get("AWS_ACCESS_KEY_ID"));
        assertEquals("aws-secret-key", varAsMap(context, "envars").get("AWS_SECRET_ACCESS_KEY"));
    }

    @Test
    public void validateHelmAddRepo() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("helm"));
        Map<String, ToolCommand> commands = ImmutableMap.of("helm/repo", new Repo());
        HelmTask task = new HelmTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "repo")
                .put("add",
                        taskVariables()
                                .put("name", "jetstack")
                                .put("url", "https://charts.jetstack.io")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        String expectedCommandLine = "helm repo add jetstack https://charts.jetstack.io";
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));
    }

    @Test
    public void validateHelmDestroy() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("helm"));
        Map<String, ToolCommand> commands = ImmutableMap.of("helm/destroy", new Destroy());
        HelmTask task = new HelmTask(commands, lockService, toolInitializer);

        Map<String, Object> args = Maps.newHashMap(taskVariables()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "destroy")
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        String expectedCommandLine = "helm list --short | xargs -L1 helm delete";
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));
    }

}
