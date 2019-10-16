package com.walmartlabs.concord.plugins.k8s.eksctl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.Cluster;
import com.walmartlabs.concord.plugins.tool.ToolCommand;
import javax.inject.Named;
import javax.inject.Singleton;

@Named("eksctl/create")
@Singleton
public class Create implements ToolCommand {

  @JsonProperty("cluster")
  private Cluster cluster;

  public Cluster cluster() {
    return cluster;
  }

}
