package com.walmartlabs.concord.plugins.k8s.eksctl.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireeye.k8s.ClusterGenerationRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

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
