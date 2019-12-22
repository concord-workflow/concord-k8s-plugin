package com.walmartlabs.concord.plugins.k8s.kubectl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.tool.Option;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;

import javax.inject.Named;

@Named("kubectl/delete")
public class Delete extends ToolCommandSupport {

    @JsonProperty
    @Option(name = {"crd"})
    private String crd;

    public String crd() {
        return crd;
    }
}
