package com.walmartlabs.concord.plugins;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import java.util.Map;

public class Configurator {

    private final ObjectMapper mapper;

    public Configurator(boolean yaml) {
        if(yaml) {
            mapper = new ObjectMapper(new YAMLFactory());
        } else {
            mapper = new ObjectMapper();
        }
        mapper.registerModule(new GuavaModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Configurator() {
        this(false);
    }

    public <T> T createConfiguration(Map<String, Object> input, Class<T> clazz) {
        JavaType type = mapper.getTypeFactory().constructType(clazz);
        return mapper.convertValue(input, type);
    }

    public <T> void configure(T objectToConfigure, Map<String, Object> configuration) throws Exception {
        mapper.readerForUpdating(objectToConfigure).readValue(mapper.writeValueAsString(configuration));
    }
}
