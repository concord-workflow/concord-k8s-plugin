package com.walmartlabs.concord.secrets.aws;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.plugins.k8s.secrets.Secret;
import com.walmartlabs.concord.plugins.k8s.secrets.SecretsManager;
import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.walmartlabs.concord.secrets.aws.Indent.indentBlock;

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

        try {
            AsmClient asmClient = new AsmClient(region, awsAccessKey, awsSecretKey);
            String organizationSecretsYaml = asmClient.get(organization);
            logger.info("Successfully retrieved the organization secrets for '{}' from {}.", organization, region);

            SecretsManager secretsManager = new SecretsManager();
            List<Secret> secrets = secretsManager.load(organizationSecretsYaml);

            Map<String, String> secretsMap = Maps.newHashMap();
            for (Secret secret : secrets) {
                secretsMap.put(secret.name(), adjust(secret.value()));
            }

            // We take the map that we created and store the secrets in the Concord context
            context.setVariable("secrets", secretsMap);
            logger.info("Successfully injected the organization secrets for '{}' into the context. A specific secret is available as '${secrets.XXX}'.", organization);
        } catch(Exception e) {
            logger.error("Failed to load '{}' secret from ASM in {}.", organization, region);
            throw e;
        }
    }

    // Total hack to get formatting correct in Helm
    private String adjust(String value) {
        if(value.contains("--BEGIN")) {
            return indentBlock(value, 6);
        }
        return value;
    }

    private String base64(String originalInput) {
        return Base64.getEncoder().encodeToString(originalInput.getBytes());
    }
}
