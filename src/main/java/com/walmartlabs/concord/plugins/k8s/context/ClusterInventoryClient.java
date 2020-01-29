package com.walmartlabs.concord.plugins.k8s.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.plugins.Configurator;
import com.walmartlabs.concord.plugins.inventory.ConcordClientSupport;
import com.walmartlabs.concord.plugins.inventory.ConcordInventoryClient;

import javax.inject.Named;
import java.util.Map;

@Named
public class ClusterInventoryClient extends ConcordClientSupport {

    private final static String INVENTORY_NAME = "k8sClusters";
    private final ConcordInventoryClient inventory;
    private final ObjectMapper mapper;
    private final Configurator configurator;

    public ClusterInventoryClient(String url, String apiKey) {
        this(createClient(url, apiKey));
    }

    public ClusterInventoryClient(ApiClient apiClient) {
        super(apiClient);
        this.inventory = new ConcordInventoryClient(apiClient);
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new GuavaModule());
        this.configurator = new Configurator();
    }

    public void createInventory(String orgName) throws Exception {
        inventory.createOrUpdate(orgName, INVENTORY_NAME);
    }

    public void updateCluster(String orgName, K8sCluster cluster) throws Exception {
        inventory.putItem(orgName, INVENTORY_NAME, cluster.id(), mapper.writeValueAsString(cluster));
    }

    public K8sCluster getCluster(String orgName, String clusterId) throws Exception {
        Map<String,Object> clusterAsMap = inventory.getItem(orgName, INVENTORY_NAME, clusterId);
        return configurator.createConfiguration(clusterAsMap, K8sCluster.class);
    }

    public boolean clusterExists(String orgName, String clusterId) throws Exception {
        Map<String,Object> clusterAsMap = inventory.getItem(orgName, INVENTORY_NAME, clusterId);
        return clusterAsMap != null;
    }

    public K8sClusters getClusters(String orgName) throws Exception {
        return mapper.readValue(mapper.writeValueAsString(inventory.getAllItems(orgName, INVENTORY_NAME)), K8sClusters.class);
    }

    // TODO: this needs to be turned into an integration test
    public static void main(String[] args) throws Exception {

        ClusterInventoryClient inventory = new ClusterInventoryClient("http://localhost:8080", "auBy4eDWrKWsyhiDp3AQiw");

        // Create the clusters inventory
        inventory.createInventory("fireeye");

        // Add an cluster item to the inventory: clusters/cluster-001
        K8sCluster c1 = ImmutableK8sCluster.builder().id("cluster-001").build();
        inventory.updateCluster("fireeye", c1);
        // Add an cluster item to the inventory: clusters/cluster-002
        K8sCluster c2 = ImmutableK8sCluster.builder().id("cluster-002").build();
        inventory.updateCluster("fireeye", c2);
        // Add an cluster item to the inventory: clusters/cluster-003
        K8sCluster c3 = ImmutableK8sCluster.builder().id("cluster-003").build();
        inventory.updateCluster("fireeye", c3);

        // Retrieve a single item in the clusters inventory: clusters/cluster-003
        K8sCluster o = inventory.getCluster("fireeye", "cluster-003");
        System.out.println(o);
        // {cluster3=testData3}

        // Return all items in the inventory: cluster
        K8sClusters clusters = inventory.getClusters("fireeye");
        for(K8sCluster cluster : clusters.getClusters().values()) {
            System.out.println(cluster);
        }
        // {clusters={cluster-003={cluster3=testData3}, cluster-002={cluster2=testData2}, cluster-001={cluster1=testData1}}}
    }
}
