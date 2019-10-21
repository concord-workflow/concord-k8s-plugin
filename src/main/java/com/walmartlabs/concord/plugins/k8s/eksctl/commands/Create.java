package com.walmartlabs.concord.plugins.k8s.eksctl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.Cluster;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Named;
import java.nio.file.Path;

@Named("eksctl/create")
public class Create extends ToolCommandSupport {

    @JsonProperty("cluster")
    private Cluster cluster;

    public Cluster cluster() {
        return cluster;
    }

    /**
     * If the idemptotency check command is executed and returns 0 the corresponding task
     * command has already run successfully.
     */
    @Override
    public String idempotencyCheckCommand() {
        return String.format("{{executable}} get cluster --name %s --region %s -o json", cluster.name(), cluster.region());
    }

    @Override
    public void postProcess(Path workDir, Context context) throws Exception {

        /*
        String kubeconfigFile = cluster.kubeconfig();
        Path kubeConfigPath = Paths.get(kubeconfigFile);
        String kubeconfigContent = new String(Files.readAllBytes(kubeConfigPath));

        // ${workDir}/.aws-iam-authenticator/aws-iam-authenticator
        String awsIamAuthenticator = String.format("%s/.aws-iam-authenticator/aws-iam-authenticator", workDir.toFile().getAbsolutePath());
        kubeconfigContent = kubeconfigContent.replaceAll("aws-iam-authenticator", awsIamAuthenticator);

        Files.write(kubeConfigPath, kubeconfigContent.getBytes(), StandardOpenOption.CREATE_NEW);
        */
    }
}
