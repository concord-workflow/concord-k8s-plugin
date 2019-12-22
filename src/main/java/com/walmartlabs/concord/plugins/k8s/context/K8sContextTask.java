package com.walmartlabs.concord.plugins.k8s.context;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
@Named("k8sContext")
public class K8sContextTask extends TaskSupport {

    private final static Logger logger = LoggerFactory.getLogger(K8sContextTask.class);
    private final ApiClientFactory clientFactory;

    @Inject
    public K8sContextTask(ApiClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public void initializeClusterInventory(Context context) throws Exception {
        String clusterId = clusterId(context);
        logger.info("Initializing cluster inventory for {}.", clusterId);
        ClusterInventoryClient client = client(context);
        client.createInventory(orgName(context));
    }

    public K8sCluster cluster(Context context) throws Exception {
        String clusterId = clusterId(context);
        String orgName = orgName(context);
        ClusterInventoryClient client = client(context);
        //
        // Always make sure the inventory for clusters has been created
        //
        client.createInventory(orgName(context));
        K8sCluster cluster = client.getCluster(orgName, clusterId);
        if(cluster == null) {
            cluster = ImmutableK8sCluster.builder()
                    .id(clusterId)
                    .build();
            client.updateCluster(orgName, cluster);
        }
        return cluster;
    }

    // --------------------------------------------------------------------------------------------------------------------
    // Ingress annotations
    // --------------------------------------------------------------------------------------------------------------------

    public void ingressAnnotation(Context context, String ingressAnnotation) throws Exception {

        logger.info("Adding ingress annotation: {}", ingressAnnotation);
        K8sCluster cluster = cluster(context);
        Set<String> updatedIngressAnnotations = Sets.newHashSet(cluster.ingressAnnotations());
        updatedIngressAnnotations.add(ingressAnnotation);
        K8sCluster updatedCluster = ImmutableK8sCluster
                .copyOf(cluster)
                .withIngressAnnotations(updatedIngressAnnotations);
        ClusterInventoryClient client = client(context);
        client.updateCluster(orgName(context), updatedCluster);
    }

    public String ingressAnnotations(Context context, int indentCount) throws Exception {
        List<String> ingressAnnotations = Lists.newArrayList(ingressAnnotations(context));
        logger.info("Retrieving {} ingress annotations with indent = {}", ingressAnnotations.size(), indentCount);
        String indent = String.join("", Collections.nCopies(indentCount, " "));
        StringBuffer sb = new StringBuffer(System.lineSeparator());
        int size = ingressAnnotations.size();
        for (int i = 0; i < size; i++) {
            String ingressAnnotation = ingressAnnotations.get(i);
            sb.append(indent).append(ingressAnnotation);
            if (i != (size - 1)) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public Set<String> ingressAnnotations(Context context) throws Exception {
        K8sCluster cluster = cluster(context);
        return cluster.ingressAnnotations();
    }

    // --------------------------------------------------------------------------------------------------------------------
    // Post manifests
    // --------------------------------------------------------------------------------------------------------------------

    public void postManifest(Context context, String manifest) throws Exception {
        logger.info("Adding post manifest: {}", manifest);
        K8sCluster cluster = cluster(context);
        Set<String> updatedPostManifests = Sets.newHashSet(cluster.postManifests());
        updatedPostManifests.add(manifest);
        K8sCluster updatedCluster = ImmutableK8sCluster
                .copyOf(cluster)
                .withPostManifests(updatedPostManifests);
        ClusterInventoryClient client = client(context);
        client.updateCluster(orgName(context), updatedCluster);
    }

    public List<String> postManifests(Context context) throws Exception {
        K8sCluster cluster = cluster(context);
        return Lists.newArrayList(cluster.postManifests());
    }

    // --------------------------------------------------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------------------------------------------------

    private ClusterInventoryClient client(Context context) {
        return new ClusterInventoryClient(apiClient(clientFactory, context));
    }

    public String clusterId(Context context) {
        return varAsString(clusterRequest(context), "clusterId");
    }

    public Map<String, Object> clusterRequest(Context context) {
        return (Map<String, Object>) context.getVariable("clusterRequest");
    }
}
