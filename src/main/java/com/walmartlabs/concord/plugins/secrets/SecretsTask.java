package com.walmartlabs.concord.plugins.secrets;

import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.ApiException;
import com.walmartlabs.concord.client.ApiClientConfiguration;
import com.walmartlabs.concord.client.ApiClientFactory;
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
import java.nio.file.StandardOpenOption;

@Named("secret")
public class SecretsTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(SecretsTask.class);

    private final ApiClientFactory apiClientFactory;
    private final SecretService secretService;

    @Inject
    public SecretsTask(ApiClientFactory apiClientFactory, SecretService secretService) {
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

        File fileContainingSecret = new File(file);
        if (!fileContainingSecret.exists()) {
            //
            // The file containing our secret is not present on this agent. We are going to retrieve it
            // and place it in the correct location.
            //
            try {
                String secretMaterial = secretService.exportAsString(context, instanceId, organization, name, null);
                if(!fileContainingSecret.getParentFile().exists()) {
                    Files.createDirectories(fileContainingSecret.getParentFile().toPath());
                }
                Files.write(fileContainingSecret.toPath(), secretMaterial.getBytes());
            } catch (Exception e) {
                throw new Exception(String.format("Error storing the secret %s to %s.", name, file));
            }
        } else {
            //
            // The file containing our secret does exist so we'll attempt to store it for subsequent use
            //
            try {
                SecretsClient secretsClient = new SecretsClient(apiClient(context));
                String fileContents = new String(Files.readAllBytes(Paths.get(file)));
                //
                // This is a total hard-coded hack for now. Generalize this handling. This is for the kubeconfig
                // to override where the aws-iam-authenticator binary is located.
                //
                fileContents = fileContents.replace("command: aws-iam-authenticator", "command: /home/concord/bin/aws-iam-authenticator");
                // Now we want to write out the new file with the replaced path
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

    public static void main(String[] args) throws Exception {
        String s = "    command: aws-iam-authenticator";

    }
}
