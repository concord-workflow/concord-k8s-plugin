package ca.vanzyl.concord.plugins.k8s.kubectl.config;

import ca.vanzyl.concord.plugins.tool.annotations.Value;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Namespace {

    @JsonProperty
    @Value
    private String name;

    public String name() {
        return name;
    }
}
