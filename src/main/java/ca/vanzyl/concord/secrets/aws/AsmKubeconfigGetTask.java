package com.walmartlabs.concord.secrets.aws;

import com.walmartlabs.concord.client.ApiClientFactory;
import ca.vanzyl.concord.plugins.k8s.K8sTaskSupport;
import ca.vanzyl.concord.plugins.secrets.ConcordSecretsClient;
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
public class AsmKubeconfigGetTask extends K8sTaskSupport {

    private final static Logger logger = LoggerFactory.getLogger(AsmKubeconfigGetTask.class);

    @Inject
    public AsmKubeconfigGetTask(ApiClientFactory apiClientFactory) {
        super(apiClientFactory);
    }

    @Override
    public void execute(Context context) throws Exception {

        if(!clusterExists(context)) {
            return;
        }

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

