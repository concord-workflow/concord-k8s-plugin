package ca.vanzyl.concord.plugins.k8s.helm.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Add {

    @JsonProperty
    private String name;

    @JsonProperty
    private String url;

    public String name() { return name; }

    public String url() { return url; }
}
