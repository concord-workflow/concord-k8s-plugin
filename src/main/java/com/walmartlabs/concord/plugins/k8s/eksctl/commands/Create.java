package com.walmartlabs.concord.plugins.k8s.eksctl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.Cluster;
import com.walmartlabs.concord.plugins.tool.ToolCommand;
import java.util.List;
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

  @Override
  public List<String> commandLineArguments() {
    List<String> arguments = Lists.newArrayList("create");
    if(cluster != null) {
      arguments.add("cluster");
      if(cluster.configFile() != null) {
        arguments.add("-f");
        arguments.add(cluster.configFile());
      }
      if(cluster.kubeConfig() != null) {
        arguments.add("--kubeconfig");
        arguments.add(cluster.kubeConfig());
      }
    }

    return arguments;
  }
}
