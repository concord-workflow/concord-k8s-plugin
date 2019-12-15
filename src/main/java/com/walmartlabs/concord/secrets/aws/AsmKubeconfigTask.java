package com.walmartlabs.concord.secrets.aws;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.plugins.secrets.ConcordSecretsClient;
import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;

import static com.walmartlabs.concord.plugins.secrets.ConcordSecretsClient.apiClient;

// Write a specified kubeconfig to ASM
@Named("asmKubeconfig")
public class AsmKubeconfigTask extends TaskSupport {

    private final static Logger logger = LoggerFactory.getLogger(AsmKubeconfigTask.class);

    private final ApiClientFactory apiClientFactory;

    @Inject
    public AsmKubeconfigTask(ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    @Override
    public void execute(Context context) throws Exception {

        String kubeconfigName = varAsString(context, "kubeconfigName");
        String awsAccessKey = varAsString(context, "awsAccessKey");
        String awsSecretKey = varAsString(context, "awsSecretKey");
        ConcordSecretsClient concordSecretsClient = new ConcordSecretsClient(apiClient(apiClientFactory, context));
        String kubeconfigContent = concordSecretsClient.secretAsString(orgName(context), kubeconfigName);

        List<String> regions = (List<String>) context.getVariable("regions");
        for(String region : regions) {
            logger.info("Attempting to write {} to ASM in {}.", kubeconfigName, region);
            AsmClient asmClient = new AsmClient(region, awsAccessKey, awsSecretKey);
            // Don't attempt to write the secret if it already exists
            if(!asmClient.secretsList().contains(kubeconfigName)) {
                asmClient.put(kubeconfigName, kubeconfigContent);
                logger.warn("The secret {} successfully written to {}.", kubeconfigName, region);

            } else {
                logger.warn("The secret {} already exists in {} and won't be overridden.", kubeconfigName, region);
            }
        }
    }
}
