package com.walmartlabs.concord.plugins.k8s.eksctl.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cluster {

  @JsonProperty
  private String configFile;

  public String configFile() {
    return configFile;
  }
}
