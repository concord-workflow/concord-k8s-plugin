package com.walmartlabs.concord.plugins.k8s.eksctl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.airlift.airline.Option;

public class Cluster {

    @JsonProperty
    @Option(name = {"--name"})
    private String name;

    @JsonProperty
    @Option(name = {"--region"})
    private String region;

    @JsonProperty
    @Option(name = {"--version"})
    private String version;

    @JsonProperty
    @Option(name = {"--config-file"})
    private String configFile;

    @JsonProperty
    @Option(name = {"--kubeconfig"})
    private String kubeconfig;

    public String name() { return name; }

    public String region() {
        return region;
    }

    public String version() {
        return version;
    }

    public String configFile() {
        return configFile;
    }

    public String kubeconfig() {
        return kubeconfig;
    }
}
