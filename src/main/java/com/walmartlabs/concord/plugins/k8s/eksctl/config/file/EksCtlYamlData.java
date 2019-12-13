package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//
// This is the information about the cluster that results after gathering VPC and EKS information from Terraform and is used by
// the EKSCtl configuration template rendering process.
//
public class EksCtlYamlData {

    private static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private String name;
    private String region;
    private String keypair;
    private String k8sVersion;
    private Map<String, Object> terraformOutput;

    public EksCtlYamlData(String name, String region, String keypair, String k8sVersion, File terraformOutput) {
        this(name, region, keypair, k8sVersion, map(terraformOutput));
    }

    public EksCtlYamlData(String name, String region, String keypair, String k8sVersion, Map<String, Object> terraformOutput) {
        this.name = name;
        this.region = region;
        this.keypair = keypair;
        this.k8sVersion = k8sVersion;
        this.terraformOutput = terraformOutput;
    }

    public String name() {
        return name;
    }

    public String region() {
        return region;
    }

    public String keypair() {
        return keypair;
    }

    public String k8sVersion() {
        return k8sVersion;
    }

    public String vpcId() {
        return (String) ((Map) ((Map) terraformOutput.get("vpc")).get("value")).get("id");
    }

    public String vpcCidr() {
        return (String) ((Map) ((Map) terraformOutput.get("vpc")).get("value")).get("cidr_block");
    }

    public String vpcName() {
        return (String) ((Map) ((Map) ((Map) terraformOutput.get("vpc")).get("value")).get("tags")).get("Name");
    }

    public String serviceRoleArn() {
        return (String) ((Map) ((Map) terraformOutput.get("eks-service-role")).get("value")).get("arn");
    }

    public String instanceProfileArn() {
        return (String) ((Map) ((Map) terraformOutput.get("eks-worker-node-instance-profile")).get("value")).get("arn");
    }

    public String instanceRoleArn() {
        return (String) ((Map) ((Map) terraformOutput.get("eks-worker-node-role")).get("value")).get("arn");
    }

    public List<Tag> tags() {
        List<Tag> tags = new ArrayList<>();
        Map<String, String> m = (Map<String, String>) (((Map) terraformOutput.get("tags")).get("value"));
        for (Map.Entry<String, String> e : m.entrySet()) {
            tags.add(new Tag(e.getKey(), e.getValue()));
        }
        return tags;
    }

    public List<Subnet> privateSubnets() {
        List<Subnet> privateSubnets = new ArrayList<>();
        Map<String, Map<String, String>> m = ((Map<String, Map<String, String>>) (((Map) terraformOutput.get("private_subnets")).get("value")));
        for (Map.Entry<String, Map<String, String>> e : m.entrySet()) {
            privateSubnets.add(subnet(e));
        }
        return privateSubnets;
    }

    public List<Subnet> publicSubnets() {
        List<Subnet> publicSubnets = new ArrayList<>();
        Map<String, Map<String, String>> m = ((Map<String, Map<String, String>>) (((Map) terraformOutput.get("public_subnets")).get("value")));
        for (Map.Entry<String, Map<String, String>> e : m.entrySet()) {
            publicSubnets.add(subnet(e));
        }
        return publicSubnets;
    }

    public Subnet subnet(Map.Entry<String, Map<String, String>> e) {
        return new Subnet(e.getKey(), e.getValue().get("id"), e.getValue().get("cidr_block"));
    }

    public static Map<String,Object> map(File jsonFile) {
        try {
            return mapper.readValue(jsonFile, Map.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String,Object> map(String json) {
        try {
            return mapper.readValue(json, Map.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Subnet {

        private String id;
        private String subnet;
        private String cidr;

        public Subnet(String id, String subnet, String cidr) {
            this.id = id;
            this.subnet = subnet;
            this.cidr = cidr;
        }

        public String id() {
            return id;
        }

        public String subnet() {
            return subnet;
        }

        public String cidr() {
            return cidr;
        }
    }

    public static class Tag {
        private String key;
        private String value;

        public Tag(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }
    }
}
