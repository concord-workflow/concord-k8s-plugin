package ca.vanzyl.concord.plugins.k8s.helm.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import ca.vanzyl.concord.plugins.tool.annotations.Flag;
import ca.vanzyl.concord.plugins.tool.annotations.Option;
import ca.vanzyl.concord.plugins.tool.ToolCommandSupport;

import javax.inject.Named;

@Named("helm/init")
public class Init extends ToolCommandSupport {

    @JsonProperty
    @Option(name = {"--service-account"})
    private String serviceAccount;

    @JsonProperty
    @Flag(name = {"--wait"})
    private boolean wait;

    public String serviceAccount() { return serviceAccount; }

    public boolean waitForCompletion() {
        return wait;
    }
}
