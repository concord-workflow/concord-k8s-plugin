package ca.vanzyl.concord.plugins.k8s.helm.config;

import ca.vanzyl.concord.plugins.tool.annotations.OptionWithEquals;
import ca.vanzyl.concord.plugins.tool.annotations.Value;
import com.fasterxml.jackson.annotation.JsonProperty;
import ca.vanzyl.concord.plugins.k8s.helm.commands.Upgrade;
import ca.vanzyl.concord.plugins.tool.annotations.KeyValue;
import ca.vanzyl.concord.plugins.tool.annotations.Option;

import java.util.List;

public class Chart {

    @JsonProperty
    @Option(name = {"--namespace"})
    private String namespace;

    @JsonProperty
    @Option(name = {"--version"})
    private String version;

    @JsonProperty
    @KeyValue(name = "--set")
    private List<String> set;

    @JsonProperty
    @Option(name = {"--values"})
    private String values;

    @JsonProperty
    @Option(name = {"--timeout"})
    private String timeout = "300";

    @JsonProperty
    @Option(name = {"--name"}, omitFor=Upgrade.class)
    private String name;

    @JsonProperty
    @Value
    private String value;

    public Chart() {
    }

    public String name() {
        return name;
    }

    public String namespace() {
        return namespace;
    }

    public String version() {
        return version;
    }

    public String value() {
        return value;
    }

    public String values() {
        return values;
    }
}
