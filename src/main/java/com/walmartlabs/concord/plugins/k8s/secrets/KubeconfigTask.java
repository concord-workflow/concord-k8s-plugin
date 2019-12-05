package com.walmartlabs.concord.plugins.k8s.secrets;

import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.ApiException;
import com.walmartlabs.concord.client.ApiClientConfiguration;
import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.plugins.secrets.ConcordSecretsClient;
import com.walmartlabs.concord.sdk.Constants;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.SecretService;
import com.walmartlabs.concord.sdk.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

//TODO:  Let's make this kubeconfig specific

@Named("kubeconfig")
public class KubeconfigTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(KubeconfigTask.class);

    private final ApiClientFactory apiClientFactory;
    private final SecretService secretService;

    @Inject
    public KubeconfigTask(ApiClientFactory apiClientFactory, SecretService secretService) {
        this.apiClientFactory = apiClientFactory;
        this.secretService = secretService;
    }

    @Override
    public void execute(Context context) throws Exception {

        // Store the secret if it doesn't exist in Concord
        // Persist it to disk if not present on the agent

        String instanceId = (String) context.getVariable(Constants.Context.TX_ID_KEY);
        String organization = (String) context.getVariable("organization");
        String name = (String) context.getVariable("name");
        String file = (String) context.getVariable("file");

        // TODO: we should always write out the secret in case it has changed to correct an issue
        File fileContainingSecret = new File(file);
        if (!fileContainingSecret.exists()) {
            //
            // The file containing our secret is not present on this agent. We are going to retrieve it
            // and place it in the correct location.
            //
            String secretMaterial = null;
            try {
                // TODO: This service swallows exceptions and just reports errors
                secretMaterial = secretService.exportAsString(context, instanceId, organization, name, null);
                if (secretMaterial != null) {
                    // We were able to retrieve the kubeconfig
                    if (!fileContainingSecret.getParentFile().exists()) {
                        Files.createDirectories(fileContainingSecret.getParentFile().toPath());
                    }
                    Files.write(fileContainingSecret.toPath(), secretMaterial.getBytes());
                }
            } catch (Exception e) {
                if(secretMaterial == null) {
                    logger.info("We are expecting a cluster creation operation to produce a kubeconfig file.");
                } else {
                    throw new Exception(String.format("Error storing the secret %s to %s.", name, file));
                }
            }
        } else {
            //
            // The file containing our secret does exist so we'll attempt to store it for subsequent use
            //
            try {
                ConcordSecretsClient secretsClient = new ConcordSecretsClient(apiClient(context));
                String fileContents = new String(Files.readAllBytes(Paths.get(file)));
                Files.write(fileContainingSecret.toPath(), fileContents.getBytes());
                secretsClient.addPlainSecret(organization, name, false, null, fileContents.getBytes());
                logger.info("The secret '{}' has been successfully stored in the {} organization.", name, organization);
            } catch (ApiException e) {
                if (e.getMessage().contains("Secret already exists")) {
                    logger.info("The secret '{}' already exists in the {} organization.", name, organization);
                }
            }
        }
    }

    private ApiClient apiClient(Context context) {
        return apiClientFactory
                .create(ApiClientConfiguration.builder()
                        .context(context)
                        .build());
    }
}
