package com.walmartlabs.concord.plugins.k8s.eksctl.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cluster {

  @JsonProperty
  private String configFile;

  @JsonProperty
  private String kubeConfig;

  public String configFile() {
    return configFile;
  }

  public String kubeConfig() {
    return kubeConfig;
  }
}
