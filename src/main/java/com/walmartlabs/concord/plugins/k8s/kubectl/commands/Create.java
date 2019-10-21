package com.walmartlabs.concord.plugins.k8s.kubectl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.kubectl.config.Namespace;
import com.walmartlabs.concord.plugins.tool.OptionWithEquals;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import io.airlift.airline.Option;

import javax.inject.Named;

@Named("kubectl/create")
public class Create extends ToolCommandSupport {

    @JsonProperty
    private Namespace namespace;

    public Namespace namespace() { return namespace; }

    // TODO: so this clearly demonstrates a problem with this model. I need an idempotency check for each
    // type the create command supports.

    @Override
    public String idempotencyCheckCommand() {
        return String.format("{{executable}} get namespace %s", namespace.name());
    }
}
