package com.walmartlabs.concord.plugins.k8s.helm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.helm.commands.Upgrade;
import com.walmartlabs.concord.plugins.tool.KeyValue;
import com.walmartlabs.concord.plugins.tool.Option;

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
    @Option(name = {"--name"}, omitFor=Upgrade.class)
    private String name;

    @JsonProperty
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
