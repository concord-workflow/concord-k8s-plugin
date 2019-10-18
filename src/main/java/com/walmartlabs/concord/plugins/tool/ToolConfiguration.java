package com.walmartlabs.concord.plugins.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ToolConfiguration {

    @JsonProperty
    private String command;

    @JsonProperty
    private String version;

    @JsonProperty
    private String url;

    @JsonProperty
    private boolean debug;

    @JsonProperty
    private boolean dryRun;

    @JsonProperty("envars")
    protected Map<String,String> envars;

    public String command() {
        return command;
    }

    public String version() {
        return version;
    }

    public String url() {
        return url;
    }

    public boolean debug() {
        return debug;
    }

    public boolean dryRun() {
        return dryRun;
    }

    public Map<String,String> envars() {
        return envars;
    }
}
