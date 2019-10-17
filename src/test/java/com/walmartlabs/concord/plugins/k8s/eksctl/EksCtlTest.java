package com.walmartlabs.concord.plugins.k8s.eksctl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.walmartlabs.TestSupport;
import com.walmartlabs.concord.plugins.k8s.eksctl.commands.Create;
import com.walmartlabs.concord.plugins.tool.ToolCommand;
import com.walmartlabs.concord.plugins.tool.ToolConfigurator;
import com.walmartlabs.concord.plugins.tool.ToolDescriptor;
import com.walmartlabs.concord.plugins.tool.ToolInitializationResult;
import com.walmartlabs.concord.plugins.tool.ToolInitializer;
import com.walmartlabs.concord.plugins.tool.ToolInitializerTest;
import com.walmartlabs.concord.plugins.tool.ToolTaskSupport;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.MockContext;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY;
import static com.walmartlabs.concord.sdk.Constants.Request.PROCESS_INFO_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EksCtlTest extends TestSupport {

    private ToolConfigurator toolConfigurator;

    @Before
    public void setUp() throws Exception {
        toolConfigurator = new ToolConfigurator();
        super.setUp();
    }

    @Test
    public void validateToolInitializerWithEksCtl() throws Exception {

        Path workingDirectory = Files.createTempDirectory("concord");
        deleteDirectory(workingDirectory);

        ToolInitializer toolInitializer = new ToolInitializer(new ToolInitializerTest.OKHttpDownloadManager("eksctl"));
        ToolDescriptor toolDescriptor = ToolTaskSupport.fromResource("eksctl");
        ToolInitializationResult result = toolInitializer.initialize(workingDirectory, toolDescriptor);

        assertTrue(result.executable().toFile().exists());
        assertTrue(Files.isExecutable(result.executable()));

        deleteDirectory(workingDirectory);
    }

    @Test
    public void validateConfiguratorUsingConfigFile() throws Exception {

        Map<String, Object> input = mapBuilder()
                .put("command", "create")
                .put("cluster",
                        mapBuilder()
                                .put("configFile", "cluster.yaml")
                                .put("kubeConfig", "/home/concord/.kube/config")
                                .build())
                .build();

        Create create = new Create();

        toolConfigurator.configureCommand(input, create);

        assertEquals("cluster.yaml", create.cluster().configFile());
        assertEquals("/home/concord/.kube/config", create.cluster().kubeConfig());
    }

    @Test
    public void validateConfiguratorUsingNameAndVersion() throws Exception {

        Map<String, Object> input = mapBuilder()
                .put("command", "create")
                .put("cluster",
                        mapBuilder()
                                .put("name", "cluster-001")
                                .put("version", "1.14")
                                .put("kubeConfig", "/home/concord/.kube/config")
                                .build())
                .build();

        Create create = new Create();

        toolConfigurator.configureCommand(input, create);

        assertEquals("cluster-001", create.cluster().name());
        assertEquals("1.14", create.cluster().version());
        assertEquals("/home/concord/.kube/config", create.cluster().kubeConfig());
    }

    @Test
    public void validateEksCtlCommandLineGenerationUsingConfig() throws Exception {

        ToolConfigurator toolConfigurator = new ToolConfigurator();

        Map<String, Object> configuration = Maps.newHashMap(mapBuilder()
                .put(PROCESS_INFO_KEY, Collections.singletonMap("sessionKey", "xyz"))
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "create")
                .put("cluster",
                        mapBuilder()
                                .put("configFile", "cluster.yaml")
                                .put("kubeConfig", "/home/concord/.kube/config")
                                .build())
                .build());

        Create create = new Create();
        toolConfigurator.configureCommand(configuration, create);
        List<String> args = ToolTaskSupport.generateCommandLineArguments("create", create);

        System.out.println(args);

        assertEquals("create", args.get(0));
        assertEquals("cluster", args.get(1));
        assertEquals("--config-file", args.get(2));
        assertEquals("cluster.yaml", args.get(3));
        assertEquals("--kubeconfig", args.get(4));
        assertEquals("/home/concord/.kube/config", args.get(5));
    }

    @Test
    public void validateEksCtlCommandLineGenerationUsingNameAndVersion() throws Exception {

        ToolConfigurator toolConfigurator = new ToolConfigurator();

        Map<String, Object> configuration = Maps.newHashMap(mapBuilder()
                .put(PROCESS_INFO_KEY, Collections.singletonMap("sessionKey", "xyz"))
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "create")
                .put("cluster",
                        mapBuilder()
                                .put("name", "cluster-001")
                                .put("version", "1.14")
                                .put("kubeConfig", "/home/concord/.kube/config")
                                .build())
                .build());

        Create create = new Create();
        toolConfigurator.configureCommand(configuration, create);
        List<String> args = ToolTaskSupport.generateCommandLineArguments("create", create);

        System.out.println(args);

        assertEquals("create", args.get(0));
        assertEquals("cluster", args.get(1));
        assertEquals("--name", args.get(2));
        assertEquals("cluster-001", args.get(3));
        assertEquals("--version", args.get(4));
        assertEquals("1.14", args.get(5));
        assertEquals("--kubeconfig", args.get(6));
        assertEquals("/home/concord/.kube/config", args.get(7));
    }

    @Test
    public void validateEksCtlTaskUsingConfigFile() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new ToolInitializerTest.OKHttpDownloadManager("eksctl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("eksctl/create", new Create());
        EksCtlTask task = new EksCtlTask(commands, lockService, toolInitializer);

        //
        // - task: eksctl
        //   in:
        //     command: create
        //     cluster:
        //       configFile: cluster.yaml
        //       kubeConfig: /home/concord/.kube/config
        //

        Map<String, Object> args = Maps.newHashMap(mapBuilder()
                .put(PROCESS_INFO_KEY, Collections.singletonMap("sessionKey", "xyz"))
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "create")
                .put("cluster",
                        mapBuilder()
                                .put("configFile", "cluster.yaml")
                                .put("kubeConfig", "/home/concord/.kube/config")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");
        System.out.println(commandLine);
        assertTrue(varAsString(context, "commandLineArguments")
                .contains("eksctl create cluster --config-file cluster.yaml --kubeconfig /home/concord/.kube/config"));
    }

    @Test
    public void validateEksCtlTaskUsingNameAndVersion() throws Exception {

        ToolInitializer toolInitializer = new ToolInitializer(new ToolInitializerTest.OKHttpDownloadManager("eksctl"));
        Map<String, ToolCommand> commands = ImmutableMap.of("eksctl/create", new Create());
        EksCtlTask task = new EksCtlTask(commands, lockService, toolInitializer);

        //
        // - task: eksctl
        //   in:
        //     command: create
        //     cluster:
        //       name: cluster-001
        //       version: 1.14
        //       kubeConfig: /home/concord/.kube/config
        //

        Map<String, Object> args = Maps.newHashMap(mapBuilder()
                .put(PROCESS_INFO_KEY, Collections.singletonMap("sessionKey", "xyz"))
                .put(WORK_DIR_KEY, workDir.toAbsolutePath().toString())
                .put("dryRun", true)
                .put("command", "create")
                .put("cluster",
                        mapBuilder()
                                .put("name", "cluster-001")
                                .put("version", "1.14")
                                .put("kubeConfig", "/home/concord/.kube/config")
                                .build())
                .build());

        Context context = new MockContext(args);
        task.execute(context);
        String commandLine = varAsString(context, "commandLineArguments");
        System.out.println(commandLine);
        assertTrue(varAsString(context, "commandLineArguments")
                .contains("eksctl create cluster --name cluster-001 --version 1.14 --kubeconfig /home/concord/.kube/config"));
    }
}
