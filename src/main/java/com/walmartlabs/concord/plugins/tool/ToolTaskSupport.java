package com.walmartlabs.concord.plugins.tool;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.LockService;
import com.walmartlabs.concord.sdk.Task;
import io.airlift.airline.Option;
import io.airlift.command.Command;
import io.airlift.command.CommandResult;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ToolTaskSupport implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ToolTaskSupport.class);

  protected final LockService lockService;
  protected final ToolInitializer toolInitializer;
  protected final ToolConfigurator toolConfigurator;
  protected final Map<String, ToolCommand> commands;

  public ToolTaskSupport(Map<String, ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
    System.out.println(commands);
    this.commands = commands;
    this.lockService = lockService;
    this.toolInitializer = toolInitializer;
    this.toolConfigurator = new ToolConfigurator();
  }

  public void execute(Context context) throws Exception {

    // Task name taken from the @Named annotation
    String taskName = this.getClass().getAnnotationsByType(Named.class)[0].value();

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

    // Apply the configuration to the command
    toolConfigurator.configureCommand(variables(context), toolCommand);

    // Initialize the specific tool and make it available to concord for use
    ToolInitializationResult toolInitializationResult = toolInitializer.initialize(workDir, toolDescriptor(taskName, toolConfiguration), toolConfiguration.debug());

    // Build up the arguments for the execution of this tool: executable +
    List<String> args = Lists.newArrayList();
    args.add(toolInitializationResult.executable().toFile().getAbsolutePath());
    args.addAll(generateCommandLineArguments(toolCommandName, toolCommand));

    Command command = new Command(args.toArray(new String[0]))
        .setDirectory(workDir.toFile())
        .setTimeLimit(20, TimeUnit.MINUTES);

    if (toolConfiguration.dryRun()) {
      String commandLineArguments = String.join(" ", command.getCommand());
      context.setVariable("commandLineArguments", String.join(" ", command.getCommand()));
      System.out.println(commandLineArguments);
    } else {
      CommandResult commandResult = command.execute(Executors.newCachedThreadPool());
      System.out.println(commandResult.getCommandOutput());
    }
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

  public static List<String> generateCommandLineArguments(String commandName, Object command) throws Exception {
    //
    // eksctl create cluster --config-file cluster.yaml --kubeconfig /home/concord/.kube/config
    //
    List<String> arguments = Lists.newArrayList(commandName);
    for (Field field : command.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      Object operand = field.get(command);
      if (operand != null) {
        arguments.add(field.getName());
        for (Field configuration : operand.getClass().getDeclaredFields()) {
          Option option = configuration.getAnnotation(Option.class);
          if (option != null) {
            configuration.setAccessible(true);
            Object value = configuration.get(operand);
            if (value != null) {
              // --config-file
              arguments.add(option.name()[0]);
              // cluster.yml
              arguments.add((String) value);
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
}
