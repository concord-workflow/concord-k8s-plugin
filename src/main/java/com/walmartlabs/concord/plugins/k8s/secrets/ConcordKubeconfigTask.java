package com.walmartlabs.concord.plugins.k8s.secrets;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.walmartlabs.concord.ApiException;
import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.plugins.secrets.ConcordSecretsClient;
import com.walmartlabs.concord.sdk.Constants;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.SecretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.walmartlabs.concord.plugins.secrets.ConcordSecretsClient.apiClient;

// TODO: just the secrets client instead of the secrets service, more control over the errors
@Named("concordKubeconfig")
public class ConcordKubeconfigTask extends TaskSupport {

    private static final Logger logger = LoggerFactory.getLogger(ConcordKubeconfigTask.class);

    private final ApiClientFactory apiClientFactory;
    private final SecretService secretService;

    @Inject
    public ConcordKubeconfigTask(ApiClientFactory apiClientFactory, SecretService secretService) {
        this.apiClientFactory = apiClientFactory;
        this.secretService = secretService;
    }

    @Override
    public void execute(Context context) throws Exception {

        String instanceId = (String) context.getVariable(Constants.Context.TX_ID_KEY);
        String organization = varAsString(context, "organization");
        String kubeconfigName = varAsString(context, "name");
        String kubeconfigFile = varAsString(context, "file");
        //
        // At this point the cluster has been created by whatever mechanism employed and we now
        // want to store the generated kubeconfig in Concord's secrets manager.
        //
        File kubeconfigFileOnAgent = new File(kubeconfigFile);
        if (kubeconfigFileOnAgent.exists()) {
            // The kubeconfig exists so we'll attempt to store it in Concord's secrets manager.
            try {
                ConcordSecretsClient secretsClient = new ConcordSecretsClient(apiClient(apiClientFactory, context));
                String fileContents = new String(Files.readAllBytes(Paths.get(kubeconfigFile)));
                Files.write(kubeconfigFileOnAgent.toPath(), fileContents.getBytes());
                secretsClient.addPlainSecret(organization, kubeconfigName, false, null, fileContents.getBytes());
                logger.info("The secret '{}' has been successfully stored in the {} organization.", kubeconfigName, organization);
            } catch (ApiException e) {
                if (e.getMessage().contains("Secret already exists")) {
                    logger.info("The secret '{}' already exists in the {} organization.", kubeconfigName, organization);
                }
            }
        } else {
            // The kubeconfig is not present on this agent. We are going to retrieve it and place it on the agent
            String secretMaterial = null;
            try {
                // TODO: This service swallows exceptions and just reports errors
                secretMaterial = secretService.exportAsString(context, instanceId, organization, kubeconfigName, null);
                if (secretMaterial != null) {
                    // We were able to retrieve the kubeconfig
                    if (!kubeconfigFileOnAgent.getParentFile().exists()) {
                        Files.createDirectories(kubeconfigFileOnAgent.getParentFile().toPath());
                    }
                    Files.write(kubeconfigFileOnAgent.toPath(), secretMaterial.getBytes());
                }
            } catch (Exception e) {
                if (secretMaterial == null) {
                    logger.info("We are expecting a cluster creation operation to produce a kubeconfig file.");
                } else {
                    throw new Exception(String.format("Error storing the secret %s to %s.", kubeconfigName, kubeconfigFile));
                }
            }
        }
    }
}
