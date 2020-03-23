package ca.vanzyl.concord.secrets.aws;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import ca.vanzyl.concord.plugins.k8s.secrets.Secret;
import ca.vanzyl.concord.plugins.k8s.secrets.SecretsManager;
import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static ca.vanzyl.concord.secrets.aws.Indent.indentBlock;

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
            if (!Strings.isNullOrEmpty(organizationSecretsYaml)) {
                logger.info("Successfully retrieved the organization secrets for '{}' from {}.", organization, region);
                SecretsManager secretsManager = new SecretsManager();
                try {
                    List<Secret> secrets = secretsManager.load(organizationSecretsYaml);
                    if (secrets != null && !secrets.isEmpty()) {
                        Map<String, String> secretsMap = Maps.newHashMap();
                        for (Secret secret : secrets) {
                            secretsMap.put(secret.name(), adjust(secret.value()));
                        }
                        // We take the map that we created and store the secrets in the Concord context
                        Map<String,String> bootstrapSecrets = (Map<String,String>) context.getVariable("bootstrap");
                        secretsMap.putAll(bootstrapSecrets);
                        context.setVariable("secrets", secretsMap);
                        logger.info("Successfully injected the organization secrets for '{}' into the context. A specific secret is available as '${secrets.XXX}'.", organization);
                    }
                } catch (IOException ioException) {
                    logger.error("Failed to load  file '{}' secret from ASM for {} in {}.", organizationSecretsYaml, organization, region);
                    logger.error("YamlLoadError ", ioException);
                }
            } else {
                logger.info("organization secrets for '{}' from {} was not found", organization, region);
            }

        } catch (Exception e) {
            logger.error("Failed to load '{}' secret from ASM in {}.", organization, region);
            throw e;
        }
    }

    // Total hack to get formatting correct in Helm
    private String adjust(String value) {
        if (value.contains("--BEGIN")) {
            return indentBlock(value, 6);
        }
        return value;
    }

    private String base64(String originalInput) {
        return Base64.getEncoder().encodeToString(originalInput.getBytes());
    }
}
