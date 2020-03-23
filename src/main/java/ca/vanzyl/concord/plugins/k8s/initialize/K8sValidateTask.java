package com.walmartlabs.concord.plugins.k8s.initialize;

import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.plugins.k8s.K8sTaskSupport;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named("k8sValidate")
public class K8sValidateTask extends K8sTaskSupport {

    @Inject
    public K8sValidateTask(ApiClientFactory apiClientFactory) {
        super(apiClientFactory);
    }

    @Override
    public void execute(Context context) throws Exception {

        // Completely validate the clusterRequest
        Map<String, Object> clusterRequest = clusterRequest(context);

        // Completely validate all necessary secrets
        Map<String, String> secrets = (Map<String, String>) context.getVariable("secrets");
    }
}
