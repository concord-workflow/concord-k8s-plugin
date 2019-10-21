package com.walmartlabs.concord.plugins.k8s.helm.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.helm.config.Chart;
import com.walmartlabs.concord.plugins.tool.Flag;
import com.walmartlabs.concord.plugins.tool.Omit;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import io.airlift.airline.Option;

import javax.inject.Named;

@Named("helm/install")
public class Install extends ToolCommandSupport {

    @JsonProperty("chart")
    @Omit
    private Chart chart;

    public Chart chart() {
        return chart;
    }

    @Override
    public String idempotencyCheckCommand() {
        return String.format("{{executable}} status %s", chart.name());
    }
}
