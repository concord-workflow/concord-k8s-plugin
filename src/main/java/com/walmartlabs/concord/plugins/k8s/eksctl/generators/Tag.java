package com.walmartlabs.concord.plugins.k8s.eksctl.generators;

public class Tag {
    private String key;
    private String value;

    public Tag(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }
}
