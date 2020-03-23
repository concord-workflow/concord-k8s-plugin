package ca.vanzyl.concord.plugins.k8s.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class K8sClusters {

    private final Map<String,K8sCluster> k8sClusters;

    @JsonCreator
    public K8sClusters(@JsonProperty("k8sClusters") Map<String, K8sCluster> k8sClusters) {
        this.k8sClusters = k8sClusters;
    }

    public Map<String,K8sCluster> getClusters() {
        return k8sClusters;
    }
}
