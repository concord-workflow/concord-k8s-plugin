package ca.vanzyl.concord.plugins.k8s.kubectl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import ca.vanzyl.concord.plugins.k8s.kubectl.config.Namespace;
import ca.vanzyl.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Named;

@Named("kubectl/create")
public class Create extends ToolCommandSupport {

    @JsonProperty
    private Namespace namespace;

    public Namespace namespace() { return namespace; }

    @Override
    public String idempotencyCheckCommand(Context context) {
        return String.format("{{executable}} get namespace %s", namespace.name());
    }
}
