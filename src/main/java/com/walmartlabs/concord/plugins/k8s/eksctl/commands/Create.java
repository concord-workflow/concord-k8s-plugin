package com.walmartlabs.concord.plugins.k8s.eksctl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.Cluster;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.Map;

@Named("eksctl/create")
public class Create extends ToolCommandSupport {

    @JsonProperty("cluster")
    private Cluster cluster;

    public Cluster cluster() {
        return cluster;
    }

    /**
     * If the idemptotency check command is executed and returns 0 the corresponding task
     * command has already run successfully.
     */
    @Override
    public String idempotencyCheckCommand(Context context) {

        String clusterName = clusterRequestVarAsString(context, "clusterName");
        String clusterRegion = clusterRequestVarAsString(context, "region");

        return String.format("{{executable}} get cluster --name %s --region %s -o json", clusterName, clusterRegion);
    }

    protected String clusterRequestVarAsString(Context context, String variable) {
        return (String) clusterRequest(context).get(variable);
    }

    protected Map<String,Object> clusterRequest(Context context) {
        return (Map<String,Object>) context.getVariable("clusterRequest");
    }
}
