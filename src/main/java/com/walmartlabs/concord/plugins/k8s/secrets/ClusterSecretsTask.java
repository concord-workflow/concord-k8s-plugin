package com.walmartlabs.concord.plugins.k8s.secrets;

import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.plugins.k8s.K8sTaskSupport;
import com.walmartlabs.concord.sdk.Constants;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.InjectVariable;
import com.walmartlabs.concord.sdk.SecretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Named("clusterSecrets")
public class ClusterSecretsTask extends K8sTaskSupport {

    private final static Logger logger = LoggerFactory.getLogger(ClusterSecretsTask.class);

    @InjectVariable(Constants.Context.CONTEXT_KEY)
    private Context context;

    private final SecretService secretService;

    @Inject
    public ClusterSecretsTask(ApiClientFactory apiClientFactory, SecretService secretService) {
        super(apiClientFactory);
        this.secretService = secretService;
    }

    public String get(@InjectVariable("txId") String instanceId, String name) throws Exception {
        String credentialsNamespace = clusterRequest(context, "credentialsNamespace");
        return get(instanceId, name, credentialsNamespace);
    }

    public String get(@InjectVariable("txId") String instanceId, String name, String credentialsNamespace) throws Exception {
        String organization = orgName(context);
        String secretName = credentialsNamespace + "-" + name;
        return secretService.exportAsString(context, instanceId, organization, secretName, null);
    }
}
