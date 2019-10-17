package com.walmartlabs.concord.plugins.k8s.eksctl.terraform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireeye.k8s.ClusterGenerationRequest;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/*
    {
        "aws-region": "us-west-2",
        "vpc-cidr":"10.189.0.0/18",
        "vpc-name":"cpie-dev-02",
        "product":"voltron",
        "environment":"dev",
        "costcenter":"platf_soar",
        "public_subnet_map" : {
            "us-west-2a" : 1,
            "us-west-2b" : 2,
            "us-west-2c" : 3
        },

        "private_subnet_map" : {
            "us-west-2a" : 4,
            "us-west-2b" : 5,
            "us-west-2c" : 6
        }
    }
*/
public class TfVarsGenerator {

    public void generate(ClusterGenerationRequest request, File file) throws IOException {
        Map<String,Object> tfvars = new LinkedHashMap();
        tfvars.put("aws-region", request.getCluster().getRegion());
        tfvars.put("vpc-cidr", request.getCluster().getVpc().getCidr());
        tfvars.put("vpc-name", request.getCluster().getVpc().getName());
        tfvars.put("product", request.getProduct().getName());
        tfvars.put("environment", request.getCluster().getEnvironment());
        tfvars.put("costcenter", request.getProduct().getCostCenter());
        int i = 1;
        Map<String,Object> publicSubnet = new LinkedHashMap<>();
        for(String s : request.getCluster().getVpc().getNetwork().getSubnets().getPublic()) {
            publicSubnet.put(s, i);
            i++;
        }
        tfvars.put("public_subnet_map", publicSubnet);
        Map<String,Object> privateSubnet = new LinkedHashMap<>();
        for(String s : request.getCluster().getVpc().getNetwork().getSubnets().getPrivate()) {
            privateSubnet.put(s, i);
            i++;
        }
        tfvars.put("private_subnet_map", privateSubnet);

        try(Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, tfvars);
        }
    }
}
