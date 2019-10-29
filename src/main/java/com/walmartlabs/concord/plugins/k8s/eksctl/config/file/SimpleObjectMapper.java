package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Map;

public class SimpleObjectMapper {

    private static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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

}
