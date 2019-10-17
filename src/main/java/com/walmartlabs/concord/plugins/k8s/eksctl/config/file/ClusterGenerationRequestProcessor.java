package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireeye.k8s.ClusterGenerationRequest;
import com.walmartlabs.concord.plugins.k8s.eksctl.terraform.TfVarsGenerator;

import java.io.*;

public class ClusterGenerationRequestProcessor {

    private final ObjectMapper yamlMapper;
    private final TfVarsGenerator tfVarsGenerator;

    public ClusterGenerationRequestProcessor() {
        yamlMapper = new YamlObjectMapperProvider().get();
        tfVarsGenerator = new TfVarsGenerator();
    }

    public ClusterGenerationRequest process(File requestFile) throws IOException {

        try (Reader reader = new InputStreamReader(new FileInputStream(requestFile))) {
            ClusterGenerationRequest request = yamlMapper.readValue(reader, ClusterGenerationRequest.class);
            File tfVarsFile = new File(requestFile.getParentFile(), request.getCluster().getName() + ".json");
            tfVarsGenerator.generate(request, tfVarsFile);
            return request;
        }
    }

    public void record(ClusterGenerationRequest request, File requestFile) throws IOException {

        ObjectMapper mapper = new YamlObjectMapperProvider().get();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(requestFile))) {
            mapper.writeValue(writer, request);
        }
    }
}
