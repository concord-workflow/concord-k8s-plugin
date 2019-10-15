package com.walmartlabs.concord.plugins.k8s.eksctl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.walmartlabs.concord.plugins.tool.ToolCommand;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;

@Named("create")
@Singleton
public class Create implements ToolCommand {

  @JsonProperty("cluster")
  private Cluster cluster;

  public Cluster cluster() {
    return cluster;
  }

  @Override
  public List<String> commandLineArguments() {
    return ImmutableList.of("create cluster", "-f", cluster.configFile());
  }
}
