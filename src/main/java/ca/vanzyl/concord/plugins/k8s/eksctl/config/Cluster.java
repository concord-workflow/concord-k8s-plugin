package ca.vanzyl.concord.plugins.k8s.eksctl.config;

import ca.vanzyl.concord.plugins.k8s.eksctl.commands.Create;
import com.fasterxml.jackson.annotation.JsonProperty;
import ca.vanzyl.concord.plugins.tool.annotations.Flag;
import ca.vanzyl.concord.plugins.tool.annotations.Option;

public class Cluster {

    @JsonProperty
    @Option(name = {"--name"})
    private String name;

    @JsonProperty
    @Option(name = {"--region"})
    private String region;

    @JsonProperty
    @Option(name = {"--version"})
    private String version;

    @JsonProperty
    @Option(name = {"--config-file"})
    private String configFile;

    @JsonProperty
    @Option(name = {"--kubeconfig"})
    private String kubeconfig;

    // There is no --wait command for the create command but there is for the delete command. If you don't use the --wait
    // flag when destroying clusters the command will return while the CloudFormation stack is being deleted and then the
    // terraform destroy call will fail because the CloudFormation stack still has an upstream dependency on the VPC.
    @JsonProperty
    @Flag(name = {"--wait"}, omitFor = Create.class)
    private boolean wait;

    public String name() {
        return name;
    }

    public String region() {
        return region;
    }

    public String version() {
        return version;
    }

    public String configFile() {
        return configFile;
    }

    public String kubeconfig() {
        return kubeconfig;
    }

    public boolean waitForCompletion() {
        return wait;
    }
}
