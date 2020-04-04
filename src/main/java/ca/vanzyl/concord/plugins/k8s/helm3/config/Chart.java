package ca.vanzyl.concord.plugins.k8s.helm3.config;

import ca.vanzyl.concord.plugins.k8s.helm3.commands.Upgrade;
import ca.vanzyl.concord.plugins.tool.annotations.KeyValue;
import ca.vanzyl.concord.plugins.tool.annotations.Option;
import ca.vanzyl.concord.plugins.tool.annotations.Value;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    @Option(name = {"-f"})
    private String values;

    @JsonProperty
    @Value
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
