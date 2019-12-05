package com.walmartlabs.concord.plugins.k8s.kubectl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.plugins.TestSupport;
import com.walmartlabs.concord.plugins.k8s.kubectl.commands.Apply;
import com.walmartlabs.concord.plugins.k8s.kubectl.commands.Create;
import com.walmartlabs.concord.plugins.tool.ToolCommand;
import com.walmartlabs.concord.plugins.tool.ToolConfigurator;
import com.walmartlabs.concord.plugins.tool.ToolDescriptor;
import com.walmartlabs.concord.plugins.tool.ToolInitializationResult;
import com.walmartlabs.concord.plugins.tool.ToolInitializer;
import com.walmartlabs.concord.plugins.tool.ToolTaskSupport;
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

public class KubeCtlTest extends TestSupport {

    private ToolConfigurator toolConfigurator;

    @Before
    public void setUp() throws Exception {
        toolConfigurator = new ToolConfigurator();
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

        Map<String, Object> input = mapBuilder()
                .put("command", "apply")
                .put("file", "00-helm/tiller-rbac.yml")
                .build();

        Apply apply = new Apply();
        toolConfigurator.configureCommand(input, apply);

        assertEquals("00-helm/tiller-rbac.yml", apply.file());
    }

    @Test
    public void validateKubeCtlApply() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("kubectl/apply", new Apply());
        KubeCtlTask task = new KubeCtlTask(commands, lockService, toolInitializer);

        //
        // - task: eksctl
        //   in:
        //     command: create
        //     cluster:
        //       name: cluster-001
        //       version: 1.14
        //       kubeconfig: /home/concord/.kube/config
        //

        Map<String, Object> args = Maps.newHashMap(mapBuilder()
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "apply")
                .put("file", "00-helm/tiller-rbac.yml")
                .put("envars",
                        mapBuilder()
                                .put("KUBECONFIG", "/workspace/_attachments/k8s-cluster0-kubeconfig")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        String expectedCommandLine = "kubectl apply -f 00-helm/tiller-rbac.yml";
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
        String commandLine = varAsString(context, "commandLineArguments");

        System.out.println(commandLine);

        String expectedCommandLine = "kubectl create namespace cert-manager";
        assertTrue(varAsString(context, "commandLineArguments").contains(expectedCommandLine));
    }
}
