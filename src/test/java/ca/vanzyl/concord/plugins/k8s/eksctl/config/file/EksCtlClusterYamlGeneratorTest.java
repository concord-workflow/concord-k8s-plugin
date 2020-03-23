package ca.vanzyl.concord.plugins.k8s.eksctl.config.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.walmartlabs.concord.plugins.ConcordTestSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EksCtlClusterYamlGeneratorTest
        extends ConcordTestSupport
{
    private String basedir;

    @Before
    public void setUp()
    {
        basedir = new File("").getAbsolutePath();
    }

    @Test
    public void validateTerraformOutputParser()
            throws Exception
    {
        EksCtlYamlData cluster = new EksCtlYamlData(new File(basedir, "src/test/resources/eksctl/terraform-output.json"));
        List<Map<String, Object>> nodeGroup = ImmutableList.of(taskVariables()
                .put("id", "eksctl")
                .put("version", "0.7.0")
                .put("nodeGroupName", "standard-worker-group-1")
                .put("instanceType", "m5.large")
                .put("desiredCapacity", "3")
                .put("minSize", "1")
                .put("maxSize", "10")
                .put("volumeSize", "200")
                .put("publicKeyName", "nodegroupkey").build());

        Map<String, Object> clusterRequest =
                taskVariables()
                        .put("profile", "jvz")
                        .put("clusterName", "magic-cluster")
                        .put("region", "us-west-2")
                        .put("user", "automation")
                        .put("k8sVersion", "1.14")
                        .put("builder",
                                taskVariables()
                                        .put("nodeGroups", nodeGroup)
                                        .build())
                        .build();

        assertEquals("vpc-0fcf234ea5579c35d", cluster.vpcId());
        assertEquals("10.189.0.0/18", cluster.vpcCidr());
        assertEquals("arn:aws:iam::502860607067:role/cpie-dev-02-eks-service-node-role", cluster.serviceRoleArn());
        assertEquals("arn:aws:iam::502860607067:instance-profile/cpie-dev-02-eks-worker-node-profile", cluster.instanceProfileArn());
        assertEquals("arn:aws:iam::502860607067:role/cpie-dev-02-eks-worker-node-role", cluster.instanceRoleArn());

        assertEquals(3, cluster.privateSubnets().size());

        assertEquals("us-west-2a", cluster.privateSubnets().get(0).id());
        assertEquals("subnet-02f6cc9e071317305", cluster.privateSubnets().get(0).subnet());
        assertEquals("10.189.32.0/21", cluster.privateSubnets().get(0).cidr());

        assertEquals("us-west-2b", cluster.privateSubnets().get(1).id());
        assertEquals("subnet-0cc30a2407fd39942", cluster.privateSubnets().get(1).subnet());
        assertEquals("10.189.40.0/21", cluster.privateSubnets().get(1).cidr());

        assertEquals("us-west-2c", cluster.privateSubnets().get(2).id());
        assertEquals("subnet-03101685cec849f0c", cluster.privateSubnets().get(2).subnet());
        assertEquals("10.189.48.0/21", cluster.privateSubnets().get(2).cidr());

        assertEquals("us-west-2a", cluster.publicSubnets().get(0).id());
        assertEquals("subnet-0c2c10d4b03349e5c", cluster.publicSubnets().get(0).subnet());
        assertEquals("10.189.8.0/21", cluster.publicSubnets().get(0).cidr());

        assertEquals("us-west-2b", cluster.publicSubnets().get(1).id());
        assertEquals("subnet-077c3442da493e911", cluster.publicSubnets().get(1).subnet());
        assertEquals("10.189.16.0/21", cluster.publicSubnets().get(1).cidr());

        assertEquals("us-west-2c", cluster.publicSubnets().get(2).id());
        assertEquals("subnet-0ecb4ed179e11c4d3", cluster.publicSubnets().get(2).subnet());
        assertEquals("10.189.24.0/21", cluster.publicSubnets().get(2).cidr());

        assertEquals("cpie-dev-02", cluster.vpcName());
        assertEquals(3, cluster.tags().size());

        assertEquals("fe_common.cost_center", cluster.tags().get(0).key());
        assertEquals("platf_soar", cluster.tags().get(0).value());

        assertEquals("fe_common.env_type", cluster.tags().get(1).key());
        assertEquals("dev", cluster.tags().get(1).value());

        assertEquals("fe_common.product", cluster.tags().get(2).key());
        assertEquals("voltron", cluster.tags().get(2).value());

        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        File clusterYml = new File(basedir, "target/cluster.yml");

        generator.generate(clusterRequest, cluster, clusterYml);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = new FileInputStream(clusterYml)) {
            Map<String, Object> yaml = mapper.readValue(inputStream, Map.class);

            assertEquals("magic-cluster", ((Map) yaml.get("metadata")).get("name"));
            assertEquals("us-west-2", ((Map) yaml.get("metadata")).get("region"));
            assertEquals("1.14", ((Map) yaml.get("metadata")).get("version"));

            assertEquals("vpc-0fcf234ea5579c35d", ((Map) yaml.get("vpc")).get("id"));
            assertEquals("10.189.0.0/18", ((Map) yaml.get("vpc")).get("cidr"));

            assertEquals("subnet-02f6cc9e071317305", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("private")).get("us-west-2a")).get("id"));
            assertEquals("10.189.32.0/21", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("private")).get("us-west-2a")).get("cidr"));

            assertEquals("subnet-0cc30a2407fd39942", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("private")).get("us-west-2b")).get("id"));
            assertEquals("10.189.40.0/21", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("private")).get("us-west-2b")).get("cidr"));

            assertEquals("subnet-03101685cec849f0c", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("private")).get("us-west-2c")).get("id"));
            assertEquals("10.189.48.0/21", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("private")).get("us-west-2c")).get("cidr"));

            assertEquals("subnet-0c2c10d4b03349e5c", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("public")).get("us-west-2a")).get("id"));
            assertEquals("10.189.8.0/21", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("public")).get("us-west-2a")).get("cidr"));

            assertEquals("subnet-077c3442da493e911", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("public")).get("us-west-2b")).get("id"));
            assertEquals("10.189.16.0/21", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("public")).get("us-west-2b")).get("cidr"));

            assertEquals("subnet-0ecb4ed179e11c4d3", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("public")).get("us-west-2c")).get("id"));
            assertEquals("10.189.24.0/21", ((Map) ((Map) ((Map) ((Map) yaml.get("vpc")).get("subnets")).get("public")).get("us-west-2c")).get("cidr"));

            assertEquals("arn:aws:iam::502860607067:role/cpie-dev-02-eks-service-node-role", ((Map) yaml.get("iam")).get("serviceRoleARN"));
            assertEquals("arn:aws:iam::502860607067:instance-profile/cpie-dev-02-eks-worker-node-profile", ((Map) ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("iam")).get("instanceProfileARN"));
            assertEquals("arn:aws:iam::502860607067:role/cpie-dev-02-eks-worker-node-role", ((Map) ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("iam")).get("instanceRoleARN"));

            assertEquals("standard-worker-group-1", ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("name"));
            assertEquals("m5.large", ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("instanceType"));
            assertEquals(3, ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("desiredCapacity"));
            assertEquals(1, ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("minSize"));
            assertEquals(10, ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("maxSize"));

            assertEquals("nodegroupkey", ((Map) ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("ssh")).get("publicKeyName"));
        }
    }

    @Test
    public void validateDefaultAMIfamilySetAsAmazonLinux2AndAMIAsStatic()
            throws Exception
    {
        EksCtlYamlData cluster = new EksCtlYamlData(new File(basedir, "src/test/resources/eksctl/terraform-output.json"));
        List<Map<String, Object>> nodeGroup = ImmutableList.of(taskVariables()
                .put("id", "eksctl")
                .put("version", "0.7.0")
                .put("nodeGroupName", "standard-worker-group-1")
                .put("instanceType", "m5.large")
                .put("desiredCapacity", "3")
                .put("minSize", "1")
                .put("maxSize", "10")
                .put("volumeSize", "200")
                .put("publicKeyName", "nodegroupkey").build());

        Map<String, Object> clusterRequest =
                taskVariables()
                        .put("profile", "jvz")
                        .put("clusterName", "magic-cluster")
                        .put("region", "us-west-2")
                        .put("user", "automation")
                        .put("k8sVersion", "1.14")
                        .put("builder",
                                taskVariables()
                                        .put("nodeGroups", nodeGroup)
                                        .build())
                        .build();

        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        File clusterYml = new File(basedir, "target/cluster.yml");

        generator.generate(clusterRequest, cluster, clusterYml);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = new FileInputStream(clusterYml)) {
            Map<String, Object> yaml = mapper.readValue(inputStream, Map.class);
            assertEquals("AmazonLinux2", ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("amiFamily"));
            assertEquals("static", ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("ami"));
        }
    }

    @Test
    public void validateAMIfamilySetAsUbuntu1804()
            throws Exception
    {
        EksCtlYamlData cluster = new EksCtlYamlData(new File(basedir, "src/test/resources/eksctl/terraform-output.json"));
        List<Map<String, Object>> nodeGroup = ImmutableList.of(taskVariables()
                .put("id", "eksctl")
                .put("version", "0.7.0")
                .put("nodeGroupName", "standard-worker-group-1")
                .put("instanceType", "m5.large")
                .put("desiredCapacity", "3")
                .put("minSize", "1")
                .put("maxSize", "10")
                .put("amiFamily", "Ubuntu1804")
                .put("volumeSize", "200")
                .put("publicKeyName", "nodegroupkey").build());

        Map<String, Object> clusterRequest =
                taskVariables()
                        .put("profile", "jvz")
                        .put("clusterName", "magic-cluster")
                        .put("region", "us-west-2")
                        .put("user", "automation")
                        .put("k8sVersion", "1.14")
                        .put("builder",
                                taskVariables()
                                        .put("nodeGroups", nodeGroup)
                                        .build())
                        .build();

        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        File clusterYml = new File(basedir, "target/cluster.yml");

        generator.generate(clusterRequest, cluster, clusterYml);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = new FileInputStream(clusterYml)) {
            Map<String, Object> yaml = mapper.readValue(inputStream, Map.class);
            assertEquals("Ubuntu1804", ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("amiFamily"));
            assertEquals("static", ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("ami"));
        }
    }

    @Test
    public void validateCustomAMI()
            throws Exception
    {
        EksCtlYamlData cluster = new EksCtlYamlData(new File(basedir, "src/test/resources/eksctl/terraform-output.json"));
        List<Map<String, Object>> nodeGroup = ImmutableList.of(taskVariables()
                .put("id", "eksctl")
                .put("version", "0.7.0")
                .put("nodeGroupName", "standard-worker-group-1")
                .put("instanceType", "m5.large")
                .put("desiredCapacity", "3")
                .put("minSize", "1")
                .put("maxSize", "10")
                .put("ami", "ami-abc-def")
                .put("volumeSize", "200")
                .put("publicKeyName", "nodegroupkey").build());

        Map<String, Object> clusterRequest =
                taskVariables()
                        .put("profile", "jvz")
                        .put("clusterName", "magic-cluster")
                        .put("region", "us-west-2")
                        .put("user", "automation")
                        .put("k8sVersion", "1.14")
                        .put("builder",
                                taskVariables()
                                        .put("nodeGroups", nodeGroup)
                                        .build())
                        .build();

        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        File clusterYml = new File(basedir, "target/cluster.yml");
        generator.generate(clusterRequest, cluster, clusterYml);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = new FileInputStream(clusterYml)) {
            Map<String, Object> yaml = mapper.readValue(inputStream, Map.class);
            assertEquals("ami-abc-def", ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("ami"));
        }
    }

    @Test
    public void validateAbsentNodeGroupKey()
            throws Exception
    {

        EksCtlYamlData cluster = new EksCtlYamlData(new File(basedir, "src/test/resources/eksctl/terraform-output.json"));

        List<Map<String, Object>> nodeGroup = ImmutableList.of(taskVariables()
                .put("id", "eksctl")
                .put("version", "0.7.0")
                .put("nodeGroupName", "standard-worker-group-1")
                .put("instanceType", "m5.large")
                .put("desiredCapacity", "3")
                .put("minSize", "1")
                .put("maxSize", "10")
                .put("volumeSize", "200").build());

        Map<String, Object> clusterRequest =
                taskVariables()
                        .put("profile", "jvz")
                        .put("clusterName", "magic-cluster")
                        .put("region", "us-west-2")
                        .put("user", "automation")
                        .put("k8sVersion", "1.14")
                        .put("builder",
                                taskVariables()
                                        .put("nodeGroups", nodeGroup)
                                        .build())
                        .build();

        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        File clusterYml = new File(basedir, "target/cluster.yml");
        generator.generate(clusterRequest, cluster, clusterYml);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = new FileInputStream(clusterYml)) {
            Map<String, Object> yaml = mapper.readValue(inputStream, Map.class);
            assertEquals("automation", ((Map) ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("ssh")).get("publicKeyName"));
        }
    }

    @Test
    public void validateClusterLoggingEnabled()
            throws Exception
    {

        EksCtlYamlData cluster = new EksCtlYamlData(new File(basedir, "src/test/resources/eksctl/terraform-output.json"));

        List<Map<String, Object>> nodeGroup = ImmutableList.of(taskVariables()
                .put("id", "eksctl")
                .put("version", "0.7.0")
                .put("nodeGroupName", "standard-worker-group-1")
                .put("instanceType", "m5.large")
                .put("desiredCapacity", "3")
                .put("minSize", "1")
                .put("maxSize", "10")
                .put("volumeSize", "200")
                .put("publicKeyName", "automation").build());

        Map<String, Object> clusterRequest =
                taskVariables()
                        .put("profile", "jvz")
                        .put("clusterName", "magic-cluster")
                        .put("region", "us-west-2")
                        .put("user", "automation")
                        .put("k8sVersion", "1.14")
                        .put("clusterLogging", true)
                        .put("builder",
                                taskVariables()
                                        .put("nodeGroups", nodeGroup)
                                        .build())
                        .build();

        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        File clusterYml = new File(basedir, "target/cluster.yml");

        generator.generate(clusterRequest, cluster, clusterYml);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = new FileInputStream(clusterYml)) {
            Map<String, Object> yaml = mapper.readValue(inputStream, Map.class);
            assertEquals("all", ((List) ((Map) ((Map) yaml.get("cloudWatch")).get("clusterLogging")).get("enableTypes")).get(0));
        }
    }

    @Test
    public void validateAbsentNodeGroupLabelShouldUseTheNodeGroupName()
            throws Exception
    {

        EksCtlYamlData cluster = new EksCtlYamlData(new File(basedir, "src/test/resources/eksctl/terraform-output.json"));

        List<Map<String, Object>> nodeGroup = ImmutableList.of(taskVariables()
                .put("id", "eksctl")
                .put("version", "0.7.0")
                .put("nodeGroupName", "standard-worker-group-1")
                .put("instanceType", "m5.large")
                .put("desiredCapacity", "3")
                .put("minSize", "1")
                .put("maxSize", "10")
                .put("volumeSize", "200").build());

        Map<String, Object> clusterRequest =
                taskVariables()
                        .put("profile", "jvz")
                        .put("clusterName", "magic-cluster")
                        .put("region", "us-west-2")
                        .put("user", "automation")
                        .put("k8sVersion", "1.14")
                        .put("builder",
                                taskVariables()
                                        .put("nodeGroups", nodeGroup)
                                        .build())
                        .build();

        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        File clusterYml = new File(basedir, "target/cluster.yml");
        generator.generate(clusterRequest, cluster, clusterYml);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = new FileInputStream(clusterYml)) {
            Map<String, Object> yaml = mapper.readValue(inputStream, Map.class);
            assertEquals("standard-worker-group-1", ((Map) ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("labels")).get("pool"));
        }
    }

    @Test
    public void validateNodeGroupLabelShouldUseTheNodeGroupLabel()
            throws Exception
    {

        EksCtlYamlData cluster = new EksCtlYamlData(new File(basedir, "src/test/resources/eksctl/terraform-output.json"));

        List<Map<String, Object>> nodeGroup = ImmutableList.of(taskVariables()
                .put("id", "eksctl")
                .put("version", "0.7.0")
                .put("nodeGroupName", "standard-worker-group-1")
                .put("instanceType", "m5.large")
                .put("desiredCapacity", "3")
                .put("minSize", "1")
                .put("maxSize", "10")
                .put("nodeGroupLabel", "ubuntu")
                .put("volumeSize", "200").build());

        Map<String, Object> clusterRequest =
                taskVariables()
                        .put("profile", "jvz")
                        .put("clusterName", "magic-cluster")
                        .put("region", "us-west-2")
                        .put("user", "automation")
                        .put("k8sVersion", "1.14")
                        .put("builder",
                                taskVariables()
                                        .put("nodeGroups", nodeGroup)
                                        .build())
                        .build();

        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        File clusterYml = new File(basedir, "target/cluster.yml");
        generator.generate(clusterRequest, cluster, clusterYml);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream inputStream = new FileInputStream(clusterYml)) {
            Map<String, Object> yaml = mapper.readValue(inputStream, Map.class);
            assertEquals("ubuntu", ((Map) ((Map) ((List) yaml.get("nodeGroups")).get(0)).get("labels")).get("pool"));
        }
    }
}
