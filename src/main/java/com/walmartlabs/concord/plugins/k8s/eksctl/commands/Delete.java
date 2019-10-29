package com.walmartlabs.concord.plugins.k8s.eksctl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.Cluster;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;

import javax.inject.Named;

@Named("eksctl/delete")
public class Delete extends ToolCommandSupport {

    @JsonProperty("cluster")
    private Cluster cluster;

    public Cluster cluster() {
        return cluster;
    }

}
