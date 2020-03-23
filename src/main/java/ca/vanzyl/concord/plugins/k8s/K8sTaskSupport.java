package ca.vanzyl.concord.plugins.k8s;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.walmartlabs.concord.client.ApiClientFactory;
import ca.vanzyl.concord.plugins.k8s.context.ClusterInventoryClient;
import com.walmartlabs.concord.sdk.Context;

import java.util.Map;

public class K8sTaskSupport extends TaskSupport {

    protected ApiClientFactory apiClientFactory;

    public K8sTaskSupport(ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    public Map<String, Object> clusterRequest(Context context) {
        return varAsMap(context, "clusterRequest");
    }

    public String clusterRequest(Context context, String name) {
        return varAsString(clusterRequest(context), name);
    }

    public String clusterId(Context context) {
        return clusterRequest(context, "clusterId");
    }

    public boolean clusterExists(Context context) throws Exception {
        ClusterInventoryClient clusterClient = new ClusterInventoryClient(apiClient(apiClientFactory, context));
        return clusterClient.clusterExists(orgName(context), clusterId(context));
    }
}
