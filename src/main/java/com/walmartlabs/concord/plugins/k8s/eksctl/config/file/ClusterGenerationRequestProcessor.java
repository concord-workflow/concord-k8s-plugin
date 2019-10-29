package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireeye.k8s.ClusterGenerationRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClusterGenerationRequestProcessor {
    private final ObjectMapper yamlMapper;

    public ClusterGenerationRequestProcessor() {
        yamlMapper = new YamlObjectMapperProvider().get();
    }

    public ClusterGenerationRequest process(File clusterRequestFile) throws IOException {

        try (Reader reader = new InputStreamReader(new FileInputStream(clusterRequestFile))) {
            ClusterGenerationRequest request = yamlMapper.readValue(reader, ClusterGenerationRequest.class);
            File tfVarsFile = new File(clusterRequestFile.getParentFile(), request.getCluster().getName() + ".json");
            generateTerraformVarsFile(request, tfVarsFile);
            return request;
        }
    }

    public ClusterGenerationRequest process(File requestFile, File tfVarsFile) throws IOException {

        try (Reader reader = new InputStreamReader(new FileInputStream(requestFile))) {
            ClusterGenerationRequest request = yamlMapper.readValue(reader, ClusterGenerationRequest.class);
            generateTerraformVarsFile(request, tfVarsFile);
            return request;
        }
    }

    public void record(ClusterGenerationRequest request, File requestFile) throws IOException {

        ObjectMapper mapper = new YamlObjectMapperProvider().get();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(requestFile))) {
            mapper.writeValue(writer, request);
        }
    }

    public void generateTerraformVarsFile(ClusterGenerationRequest request, File file) throws IOException {

        Map<String,Object> tfvars = new LinkedHashMap();
        tfvars.put("aws-region", request.getCluster().getRegion());
        tfvars.put("vpc-cidr", request.getCluster().getVpc().getCidr());
        // Base the vpc name off the identity
        String vpcName = String.format("vpc-%s-%s-%s", request.getCluster().getName(), request.getCluster().getEnvironment(), request.getCluster().getRegion());
        tfvars.put("vpc-name", vpcName);
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
