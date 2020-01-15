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

        AsmClient asmClient = new AsmClient(region, awsAccessKey, awsSecretKey);
        String organizationSecretsYaml = asmClient.get(organization);
        logger.info("Successfully retrieved the organization secrets for '{}' from {}.", organization, region);

        SecretsManager secretsManager = new SecretsManager();
        List<Secret> secrets = secretsManager.load(organizationSecretsYaml);

        Map<String, String> secretsMap = Maps.newHashMap();
        for(Secret secret : secrets) {
            secretsMap.put(secret.name(), adjust(secret.value()));
        }

        // We take the map that we created and store the secrets in the Concord context
        context.setVariable("secrets", secretsMap);
        logger.info("Successfully injected the organization secrets for '{}' into the context. A specific secret is available as '${secrets.XXX}'.", organization);

        /*

        // How to pick the right namespace...

        Path kubeconfigFile = Paths.get((String)context.getVariable("kubeconfigFile"));
        K8sSecretsClient k8sSecretsClient = new K8sSecretsClient(kubeconfigFile.toFile());

        for (Map.Entry<String,String> secret: secretsMap.entrySet()) {
            String name = secret.getKey();
            String value = secret.getValue();
            if(value != null) {
                Map<String, String> secretData = Maps.newHashMap();
                // base64 encode the value as the k8s client doesn't do this by default (which is misleading from their tests)
                secretData.put(name, base64(value));
                // Send the secret to the cluster with the given namespace
                k8sSecretsClient.addSecret(k8sNamespace, name, secretData);
            }
        }
         */
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
