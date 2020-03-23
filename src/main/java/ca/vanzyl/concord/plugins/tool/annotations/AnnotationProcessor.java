package ca.vanzyl.concord.plugins.tool.annotations;

import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class AnnotationProcessor
{
    // TODO: clean up repeated code and come up with a model for generating this
    // TODO: separate this code into its own class
    // TODO: ignore private static final fields
    public static List<String> cliArgumentsFromAnnotations(String commandName, Object command) throws Exception {
        //
        // eksctl create cluster --config-file cluster.yaml --kubeconfig /home/concord/.kube/config
        //
        // kubectl apply -f 00-helm/tiller-rbac.yml
        //
        List<String> arguments = Lists.newArrayList();

        Value v = command.getClass().getSuperclass().getAnnotation(Value.class);
        if (v == null) {
            v = command.getClass().getAnnotation(Value.class);
        }
        if (v != null) {
            arguments.add(v.value());
        } else {
            arguments.add(commandName);
        }

        for (Field field : command.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
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
                    } else if (field.getAnnotation(Value.class) != null) {
                        System.out.println("field.getName() = " + field.getName());
                        Object value = field.get(command);
                        System.out.println("value = " + value);
                        arguments.add((String) value);
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
                    // helm install <omit chart> --name xxx --values yyy
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
                                if (!option.omitFor().equals(command.getClass())) {
                                    arguments.add(option.name()[0]);
                                }
                                arguments.add((String) value);
                            }
                        } else if (configuration.getAnnotation(KeyValue.class) != null) {
                            KeyValue annotion = configuration.getAnnotation(KeyValue.class);
                            configuration.setAccessible(true);
                            Object fieldValue = configuration.get(operand);
                            if (fieldValue != null) {
                                // --set
                                String parameter = annotion.name();
                                List<String> kvs = (List<String>) fieldValue;
                                for (String e : kvs) {
                                    // --set "ingress.hostname=bob.fetesting.com"
                                    arguments.add(parameter);
                                    arguments.add(e);
                                }
                            }
                        } else {
                            Flag flag = configuration.getAnnotation(Flag.class);
                            if (flag != null) {
                                configuration.setAccessible(true);
                                boolean value = (boolean) configuration.get(operand);
                                if (value) {
                                    arguments.add(flag.name()[0]);
                                }
                            } else {
                                System.out.println("configuration = " + configuration);
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
}
