package com.walmartlabs.concord.plugins.k8s.eksctl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.tool.ToolConfiguration;

public class EksCtlConfiguration extends ToolConfiguration {

  @JsonProperty
  private Create create;

  public Create create() {
    return create;
  }

}
