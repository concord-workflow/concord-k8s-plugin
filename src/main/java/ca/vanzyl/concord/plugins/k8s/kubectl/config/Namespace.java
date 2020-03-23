package com.walmartlabs.concord.plugins.k8s.kubectl.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Namespace {

    @JsonProperty
    private String name;

    public String name() {
        return name;
    }
}
