package com.walmartlabs.concord.plugins.k8s.kubectl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.Cluster;
import com.walmartlabs.concord.plugins.tool.OptionWithEquals;
import com.walmartlabs.concord.plugins.tool.ToolCommand;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import io.airlift.airline.Option;

import javax.inject.Named;
import javax.inject.Singleton;

@Named("kubectl/apply")
public class Apply extends ToolCommandSupport {

    @JsonProperty
    @OptionWithEquals(name = {"--validate"})
    private Boolean validate;

    @JsonProperty
    @Option(name = {"-f"})
    private String file;

    public Boolean validate() { return validate; }

    public String file() { return file; }
}
