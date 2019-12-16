package com.walmartlabs.concord.secrets.aws;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.plugins.k8s.secrets.Secret;
import com.walmartlabs.concord.plugins.k8s.secrets.SecretsManager;
import com.walmartlabs.concord.plugins.secrets.ConcordSecretsClient;
import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.walmartlabs.concord.plugins.secrets.ConcordSecretsClient.apiClient;

// TODO: probably best to transfer the secrets into Concord and use them from there
@Named("asmSecrets")
public class AsmSecretsTask extends TaskSupport {

    private static Logger logger = LoggerFactory.getLogger(AsmSecretsTask.class);

    @Override
    public void execute(Context context) throws Exception {

        String region = varAsString(context, "homeRegion");
        String awsAccessKey = varAsString(context, "awsAccessKey");
        String awsSecretKey = varAsString(context, "awsSecretKey");
        String organization = orgName(context);

        AsmClient asmClient = new AsmClient(region, awsAccessKey, awsSecretKey);
        String organizationSecretsYaml = asmClient.get(organization);
        logger.info("Successfully retrieved the organization secrets for '{}' from {}.", organization, region);

        SecretsManager secretsManager = new SecretsManager();
        List<Secret> secrets = secretsManager.load(organizationSecretsYaml);

        Map<String, String> secretsMap = secrets.stream()
                .collect(Collectors.toMap(Secret::name, Secret::value));

        context.setVariable("secrets", secretsMap);
        logger.info("Successfully injected the organization secrets for '{}' into the context. A specific secret is available as '${secrets.XXX}'.", organization);
    }
}
