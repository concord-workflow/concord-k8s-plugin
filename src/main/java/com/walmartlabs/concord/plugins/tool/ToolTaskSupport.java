package com.walmartlabs.concord.plugins.tool;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.LockService;
import com.walmartlabs.concord.sdk.Task;
import io.airlift.airline.Option;
import io.airlift.units.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class ToolTaskSupport implements Task {

    private static final Logger logger = LoggerFactory.getLogger(ToolTaskSupport.class);

    protected final LockService lockService;
    protected final ToolInitializer toolInitializer;
    protected final ToolConfigurator toolConfigurator;
    protected final Map<String, ToolCommand> commands;

    public ToolTaskSupport(Map<String, ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
        this.commands = commands;
        this.lockService = lockService;
        this.toolInitializer = toolInitializer;
        this.toolConfigurator = new ToolConfigurator();
    }

    public void execute(Context context) throws Exception {

        // Task name taken from the @Named annotation. Inside of Guice a generate wrapper is created, but for testing
        // where we are not wiring with Guice we need to look at the class directly.
        Named named = this.getClass().getSuperclass().getAnnotation(Named.class);
        if (named == null) {
            named = this.getClass().getAnnotation(Named.class);
        }
        String taskName = named.value();

        Path workDir = Paths.get((String) context.getVariable(com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY));
        if (workDir == null) {
            throw new IllegalArgumentException("Can't determine the current '" + com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY + "'");
        }

        // Retrieve the name of the command from the configuration
        String toolCommandName = (String) context.getVariable("command");

        // Retrieve the configuration as a createConfiguration from the context
        Map<String, Object> configurationAsMap = variables(context);

        // Retrieve the common configuration elements for all commands:
        //
        // version
        // url
        // debug
        // dryRun
        //
        ToolConfiguration toolConfiguration = toolConfigurator.createConfiguration(configurationAsMap, ToolConfiguration.class);

        // Retrieve the specific command as specified by the "command" key in the configuration
        ToolCommand toolCommand = commands.get(taskName + "/" + toolCommandName);

        if (toolCommand == null) {
            throw new RuntimeException(String.format("Cannot find the command '%s'/%s'", taskName, toolCommandName));
        }

        // Apply the configuration to the command
        toolConfigurator.configureCommand(variables(context), toolCommand);

        ToolDescriptor toolDescriptor = toolDescriptor(taskName, toolConfiguration);

        // Initialize the specific tool and make it available to concord for use
        ToolInitializationResult toolInitializationResult = toolInitializer.initialize(workDir, toolDescriptor, toolConfiguration.debug());

        logger.info("We have successfully initialized {} version {}.", toolDescriptor.name(), toolDescriptor.version() != null ? toolDescriptor.version() : toolDescriptor.defaultVersion());

        // Build up the arguments for the execution of this tool: executable +
        List<String> args = Lists.newArrayList();
        args.add(toolInitializationResult.executable().toFile().getAbsolutePath());
        args.addAll(generateCommandLineArguments(toolCommandName, toolCommand));
        CliCommand command = new CliCommand(
                args,
                ImmutableSet.of(0),
                workDir,
                toolConfiguration.envars(),
                Duration.succinctDuration(20, TimeUnit.MINUTES)
        );

        if (toolCommand.idempotencyCheckCommand(context) != null) {

            String idempotencyCheckCommand = toolCommand.idempotencyCheckCommand(context);
            idempotencyCheckCommand = mustache(idempotencyCheckCommand, "executable", toolInitializationResult.executable().toFile().getAbsolutePath());
            logger.info("idempotencyCheckCommand: " + idempotencyCheckCommand);

            //
            // "{{executable}} get cluster --name {{name}} --region {{region}} -o json"
            //
            CliCommand idempotencyCheck = new CliCommand(
                    Lists.newArrayList(idempotencyCheckCommand.split(" ")),
                    ImmutableSet.of(0),
                    workDir,
                    toolConfiguration.envars(),
                    Duration.succinctDuration(20, TimeUnit.MINUTES));

            CliCommand.Result result = idempotencyCheck.execute(Executors.newCachedThreadPool());

            if (result.getCode() == 0) {

                logger.info("This command has already run successfully: " + command.getCommand());
                //
                // The task we are intending to run has already executed successfully. It is the job of the idempotency
                // command to ask if what we intend to do has already been done.
                //
                return;
            }
        }

        // Here is where we want to alter what Helm install is doing. If there is an externals configuration we want
        // fetch the Helm chart, insert the externals into the Helm chart and then install from the directory we
        // created with the fetched Helm chart
        //
        // - do any preparation work here and run any commands necessary. i need the path to the executable and access
        //   to the command and its configuration
        // - change the command line arguments as necessary. in the case of Helm we need to install from the directory
        //   just created.

        // Place envar into the context
        context.setVariable("envars", toolConfiguration.envars());

        String commandLineArguments = String.join(" ", command.getCommand());
        if (toolConfiguration.dryRun()) {
            context.setVariable("commandLineArguments", String.join(" ", command.getCommand()));
            logger.info(commandLineArguments);
        } else {
            logger.info("Executing: " + commandLineArguments);
            logger.info("Envars: " + command.getEnvironment());
            CliCommand.Result commandResult = command.execute(Executors.newCachedThreadPool());
            logger.info("commandResult.getCode() = " + commandResult.getCode());
            logger.info("commandResult.getStderr() = " + commandResult.getStderr());
            logger.info("commandResult.getStdout() = " + commandResult.getStdout());
            //logger.info(commandResult.getCommandOutput());

            // If there is some post-processing this command wants to execute then do so
            toolCommand.postProcess(workDir, context);
        }
    }

    // TODO: clean up repeated code and come up with a model for generating this
    public static List<String> generateCommandLineArguments(String commandName, Object command) throws Exception {
        //
        // eksctl create cluster --config-file cluster.yaml --kubeconfig /home/concord/.kube/config
        //
        // kubectl apply -f 00-helm/tiller-rbac.yml
        //
        List<String> arguments = Lists.newArrayList(commandName);
        for (Field field : command.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object operand = field.get(command);
            if (operand != null) {
                if (operand.getClass().isPrimitive() || Boolean.class.isAssignableFrom(operand.getClass()) || String.class.isAssignableFrom(operand.getClass())) {
                    Option option = field.getAnnotation(Option.class);
                    if (option != null) {
                        Object value = field.get(command);
                        if (value != null) {
                            arguments.add(option.name()[0]);
                            arguments.add((String) value);
                        }
                    } else {
                        Flag flag = field.getAnnotation(Flag.class);
                        if (flag != null) {
                            arguments.add(flag.name()[0]);
                        }
                        OptionWithEquals optionWithEquals = field.getAnnotation(OptionWithEquals.class);
                        if (optionWithEquals != null) {
                            Object value = field.get(command);
                            if (value != null) {
                                arguments.add(optionWithEquals.name()[0] + "=" + value);
                            }
                        }
                    }
                } else {
                    Omit omit = field.getAnnotation(Omit.class);
                    if (omit == null) {
                        arguments.add(field.getName());
                    }
                    for (Field configuration : operand.getClass().getDeclaredFields()) {
                        Option option = configuration.getAnnotation(Option.class);
                        if (option != null) {
                            configuration.setAccessible(true);
                            Object value = configuration.get(operand);
                            if (value != null) {
                                arguments.add(option.name()[0]);
                                arguments.add((String) value);
                            }
                        } else {
                            Flag flag = configuration.getAnnotation(Flag.class);
                            if (flag != null) {
                                arguments.add(flag.name()[0]);
                            } else {
                                configuration.setAccessible(true);
                                Object value = configuration.get(operand);
                                arguments.add((String) value);
                            }
                        }
                    }
                }
            }
        }
        return arguments;
    }

    public static ToolDescriptor fromResource(String taskName) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = ToolTaskSupport.class.getClassLoader().getResourceAsStream(taskName + "/" + "descriptor.yml")) {
            return mapper.readValue(inputStream, ToolDescriptor.class);
        }
    }

    private String mustache(String s, String var, String replacement) {
        return s.replaceAll("\\{\\{" + var + "\\}\\}", replacement);
    }

    protected Map<String, Object> variables(Context context) {
        Map<String, Object> variables = Maps.newHashMap();
        for (String key : context.getVariableNames()) {
            variables.put(key, context.getVariable(key));
        }
        return variables;
    }

    protected ToolDescriptor toolDescriptor(String taskName, ToolConfiguration toolConfiguration) throws Exception {

        ToolDescriptor toolDescriptor = fromResource(taskName);

        // Update the version if overriden by the user
        if (toolConfiguration.version() != null) {
            toolDescriptor = ImmutableToolDescriptor.copyOf(toolDescriptor).withVersion(toolConfiguration.version());
        }

        // Update the url if overriden by the user
        if (toolConfiguration.url() != null) {
            toolDescriptor = ImmutableToolDescriptor.copyOf(toolDescriptor).withUrlTemplate(toolConfiguration.url());
        }

        return toolDescriptor;
    }

    protected String processId(Context context) {
        return (String) context.getVariable(com.walmartlabs.concord.sdk.Constants.Context.TX_ID_KEY);
    }
}
