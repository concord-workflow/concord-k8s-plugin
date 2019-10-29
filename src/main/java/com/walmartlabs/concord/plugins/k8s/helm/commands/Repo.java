package com.walmartlabs.concord.plugins.k8s.helm.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.helm.config.Add;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;

import javax.inject.Named;

@Named("helm/repo")
public class Repo extends ToolCommandSupport {

    @JsonProperty
    private Add add;

    public Add add() { return add; }
}
