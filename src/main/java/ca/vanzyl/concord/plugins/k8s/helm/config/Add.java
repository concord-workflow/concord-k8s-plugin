package ca.vanzyl.concord.plugins.k8s.helm.config;

import ca.vanzyl.concord.plugins.tool.annotations.Value;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Add {

    @JsonProperty
    @Value
    private String name;

    @JsonProperty
    @Value
    private String url;

    public String name() { return name; }

    public String url() { return url; }
}
