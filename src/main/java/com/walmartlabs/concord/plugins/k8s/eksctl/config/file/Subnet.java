package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

public class Subnet {

    private String id;
    private String subnet;
    private String cidr;

    public Subnet(String id, String subnet, String cidr) {
        this.id = id;
        this.subnet = subnet;
        this.cidr = cidr;
    }

    public String id() {
        return id;
    }

    public String subnet() {
        return subnet;
    }

    public String cidr() {
        return cidr;
    }
}
