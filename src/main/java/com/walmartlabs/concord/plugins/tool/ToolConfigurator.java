package com.walmartlabs.concord.plugins.tool;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ToolConfigurator {

    protected final ObjectMapper mapper;

    public ToolConfigurator() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T extends ToolConfiguration> T createConfiguration(Map<String, Object> input, Class<?> clazz) {
        JavaType type = mapper.getTypeFactory().constructType(clazz);
        return mapper.convertValue(input, type);
    }

    public void configureCommand(Map<String, Object> configuration, ToolCommand command) throws Exception {
        System.out.println(configuration);
        mapper.readerForUpdating(command).readValue(mapper.writeValueAsString(configuration));
    }

}
