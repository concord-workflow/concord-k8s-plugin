package com.walmartlabs.concord.plugins.k8s.helm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.tool.KeyValue;
import io.airlift.airline.Option;

import java.util.List;
import java.util.Map;

public class Chart {

    @JsonProperty
    @Option(name = {"--name"})
    private String name;

    @JsonProperty
    @Option(name = {"--namespace"})
    private String namespace;

    @JsonProperty
    @Option(name = {"--version"})
    private String version;

    @JsonProperty
    @Option(name = {"--values"})
    private String values;

    @JsonProperty
    @KeyValue(name = "--set")
    private List<String> set;

    @JsonProperty
    private String value;

    public String name() { return name; }

    public String namespace() { return namespace; }

    public String version() { return version; }

    public String value() { return value; }

    public String values() { return values; }
}
