package ca.vanzyl.concord.plugins.k8s.helm.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Update {

    @JsonProperty
    private String name;

    public String name() { return name; }

}
