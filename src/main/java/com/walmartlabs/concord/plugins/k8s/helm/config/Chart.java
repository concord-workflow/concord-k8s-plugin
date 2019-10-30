package com.walmartlabs.concord.plugins.k8s.helm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.airlift.airline.Option;

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
    private String value;

    public String name() { return name; }

    public String namespace() { return namespace; }

    public String version() { return version; }

    public String value() { return value; }

    public String values() { return values; }
}
