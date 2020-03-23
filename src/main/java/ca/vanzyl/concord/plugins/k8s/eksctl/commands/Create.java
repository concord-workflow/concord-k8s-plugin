package ca.vanzyl.concord.plugins.k8s.eksctl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import ca.vanzyl.concord.plugins.k8s.eksctl.config.Cluster;
import ca.vanzyl.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Named;

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
}
