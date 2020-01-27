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

/**
 * Retrieving the specified kubeconfig from ASM and storing it in Concord's secret store.
 */
@Named("asmKubeconfigGet")
public class AsmKubeconfigGetTask extends TaskSupport {

    private final static Logger logger = LoggerFactory.getLogger(AsmKubeconfigGetTask.class);

    private final ApiClientFactory apiClientFactory;

    @Inject
    public AsmKubeconfigGetTask(ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    @Override
    public void execute(Context context) throws Exception {

        String kubeconfigName = varAsString(context, "kubeconfigName");
        String awsAccessKey = varAsString(context, "awsAccessKey");
        String awsSecretKey = varAsString(context, "awsSecretKey");
        ConcordSecretsClient concordSecretsClient = new ConcordSecretsClient(apiClient(apiClientFactory, context));

        List<String> regions = (List<String>) context.getVariable("regions");
        for (String region : regions) {
            logger.info("Attempting to get {} from ASM in {}.", kubeconfigName, region);
            try {
                AsmClient asmClient = new AsmClient(region, awsAccessKey, awsSecretKey);
                String kubeconfigContent = asmClient.get(kubeconfigName);
                if(concordSecretsClient.binaryDataSecret(orgName(context), kubeconfigName) == null) {
                    concordSecretsClient.addPlainSecret(orgName(context), kubeconfigName, false, null, kubeconfigContent.getBytes());
                }
                logger.info("Kubeconfig '{}' successfully transferred from {} to Concord's secret store.", kubeconfigName, region);
                return;
            } catch(Exception e) {
                logger.info("Kubeconfig '{}' not found in  {}.", kubeconfigName, region);
            }
        }
        throw new Exception("Kubeconfig '{}' not found in any of the specified regions.");
    }
}

