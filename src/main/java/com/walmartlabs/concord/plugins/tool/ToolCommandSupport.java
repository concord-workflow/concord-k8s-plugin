package com.walmartlabs.concord.plugins.tool;

import com.google.common.collect.Lists;
import io.airlift.airline.Option;
import java.lang.reflect.Field;
import java.util.List;

public abstract class ToolCommandSupport implements ToolCommand {

  public List<String> generateCommandLineArguments(String commandName) throws Exception {
    //
    // eksctl create cluster --config-file cluster.yaml --kubeconfig /home/concord/.kube/config
    //
    List<String> arguments = Lists.newArrayList(commandName);
    for (Field field : this.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      Object operand = field.get(this);
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
}
