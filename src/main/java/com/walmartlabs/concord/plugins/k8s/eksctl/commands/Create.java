package com.walmartlabs.concord.plugins.k8s.eksctl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.Cluster;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Named;
import java.nio.file.Path;

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

        String clusterName;
        String clusterRegion;
        if(cluster.name() != null && cluster.region() != null)  {
            clusterName = cluster.name();
            clusterRegion = cluster.region();
        } else {
            clusterName = variableAsString(context, "clusterName");
            clusterRegion = variableAsString(context, "region");
        }

        return String.format("{{executable}} get cluster --name %s --region %s -o json", clusterName, clusterRegion);
    }
}
