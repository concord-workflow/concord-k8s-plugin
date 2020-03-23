package ca.vanzyl.concord.plugins.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

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
        return envars != null ? envars : ImmutableMap.of();
    }

    @Override
    public String toString() {
        return "ToolConfiguration{" +
                "command='" + command + '\'' +
                ", version='" + version + '\'' +
                ", url='" + url + '\'' +
                ", debug=" + debug +
                ", dryRun=" + dryRun +
                ", envars=" + envars +
                '}';
    }
}
