package com.walmartlabs.concord.plugins.k8s.eksctl.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireeye.k8s.Cluster;
import com.fireeye.k8s.ClusterGenerationRequest;
import com.fireeye.k8s.Network;
import com.fireeye.k8s.Product;
import com.fireeye.k8s.Subnets;
import com.fireeye.k8s.Vpc;
import com.google.common.collect.ImmutableList;
import com.walmartlabs.TestSupport;
import com.walmartlabs.concord.plugins.k8s.eksctl.generators.ClusterGenerationRequestProcessor;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ClusterRequestGeneratorTest extends TestSupport {

    @Test
    public void validateClusterRequestGenerator() throws Exception {

        String clusterName = "cluster-003";

        ClusterGenerationRequestProcessor processor = new ClusterGenerationRequestProcessor();

        ClusterGenerationRequest request = new ClusterGenerationRequest()
                .withProduct(new Product()
                        .withCostCenter("costCenter")
                        .withName("product"))
                .withCluster(new Cluster()
                        .withProvider("aws")
                        .withEnvironment("dev")
                        .withName(clusterName)
                        .withRegion("us-west-2")
                        .withK8sVersion("1.14")
                        .withVpc(new Vpc()
                                .withCidr("10.189.0.0/18")
                                .withName("cpie-dev-02")
                                .withNetwork(new Network()
                                        .withSubnets(new Subnets()
                                                .withPublic(ImmutableList.of("public-us-west-2a", "public-us-west-2b", "public-us-west-2c"))
                                                .withPrivate(ImmutableList.of("private-us-west-2a", "private-us-west-2b", "private-us-west-2c"))))));

        File requestFile = target(clusterName + ".yml");

        // Write the cluster generation request to disk
        processor.record(request, requestFile);

        // Read it in as if a user had created it
        ClusterGenerationRequest requestFromDisk = processor.process(requestFile);

        assertEquals("product", requestFromDisk.getProduct().getName());
        assertEquals("costCenter", requestFromDisk.getProduct().getCostCenter());
        assertEquals("aws", requestFromDisk.getCluster().getProvider());
        assertEquals("dev", requestFromDisk.getCluster().getEnvironment());
        assertEquals(clusterName, requestFromDisk.getCluster().getName());
        assertEquals("us-west-2", requestFromDisk.getCluster().getRegion());
        assertEquals("1.14", requestFromDisk.getCluster().getK8sVersion());
        assertEquals("10.189.0.0/18", requestFromDisk.getCluster().getVpc().getCidr());
        assertEquals("cpie-dev-02", requestFromDisk.getCluster().getVpc().getName());
        assertEquals(ImmutableList.of("public-us-west-2a", "public-us-west-2b", "public-us-west-2c"), requestFromDisk.getCluster().getVpc().getNetwork().getSubnets().getPublic());
        assertEquals(ImmutableList.of("private-us-west-2a", "private-us-west-2b", "private-us-west-2c"), requestFromDisk.getCluster().getVpc().getNetwork().getSubnets().getPrivate());

        File tfVarsFile = new File(requestFile.getParentFile(), clusterName + ".json");
        Map<String, Object> tfVarsMap = new ObjectMapper().readValue(tfVarsFile, Map.class);
        assertEquals("us-west-2", tfVarsMap.get("aws-region"));
        assertEquals("10.189.0.0/18", tfVarsMap.get("vpc-cidr"));
        assertEquals("cpie-dev-02", tfVarsMap.get("vpc-name"));
        assertEquals("product", tfVarsMap.get("product"));
        assertEquals("dev", tfVarsMap.get("environment"));
        assertEquals("costCenter", tfVarsMap.get("costcenter"));
        assertEquals(1, ((Map) tfVarsMap.get("public_subnet_map")).get("public-us-west-2a"));
        assertEquals(2, ((Map) tfVarsMap.get("public_subnet_map")).get("public-us-west-2b"));
        assertEquals(3, ((Map) tfVarsMap.get("public_subnet_map")).get("public-us-west-2c"));
        assertEquals(4, ((Map) tfVarsMap.get("private_subnet_map")).get("private-us-west-2a"));
        assertEquals(5, ((Map) tfVarsMap.get("private_subnet_map")).get("private-us-west-2b"));
        assertEquals(6, ((Map) tfVarsMap.get("private_subnet_map")).get("private-us-west-2c"));
    }
}
