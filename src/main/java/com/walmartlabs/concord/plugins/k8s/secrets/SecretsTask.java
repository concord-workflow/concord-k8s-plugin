package com.walmartlabs.concord.plugins.k8s.secrets;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.client.ApiClientConfiguration;
import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.sdk.Constants;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.SecretService;
import com.walmartlabs.concord.sdk.Task;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

// This is in a K8s tree but this is strictly for interacting with Concord's secrets store.

@Named("k8sSecretSync")
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
        //
        // - task: k8sSecretSync
        //   in:
        //     organization: "${projectInfo.orgName}"
        //     cluster: ajay-003
        //     secrets:
        //       - name: fluentbit
        //         namespace: default
        //         data:
        //           key: fluentbitKey
        //           cert: fluentbitCert
        //           shared: fluentbitSharedKey
        //


        SecretsClient secretsClient = new SecretsClient(new File("/Users/jvanzyl/.kube/kubeconfig-ajay-003"));

        // Concord
        String instanceId = (String) context.getVariable(Constants.Context.TX_ID_KEY);
        String organization = (String) context.getVariable("organization");
        List<Map<String,Object>> secrets = (List<Map<String,Object>>) context.getVariable("secrets");

        // K8s
        String k8sNamespace = (String) context.getVariable("namespace");
        Map<String, String> secretData = Maps.newHashMap();

        for (Map<String,Object> secret : secrets) {
            String secretName = (String) secret.get("name");
            Map<String,String> data = (Map<String,String>) secret.get("data");
            for(Map.Entry<String,String> e : data.entrySet()) {
                // The key used for the secret in k8s
                String key = e.getKey();
                // The value is what we take from Concord's secret store using the entry value as the key for the secret to extract
                String value = secretService.exportAsString(context, instanceId, organization, e.getValue(), null);
                if(value != null) {
                    // base64 encode the value as the k8s client doesn't do this by default (which is misleading from their tests)
                    secretData.put(key, base64(value));
                    // Send the secret to the cluster with the given namespace
                    secretsClient.addSecret(k8sNamespace, secretName, secretData);
                } else {
                    logger.error("We cannot find the secret '%s' in the organization '%s' to sync to the k8s cluster.", e.getValue(), organization);
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

    private String base64(String originalInput) {
        return Base64.getEncoder().encodeToString(originalInput.getBytes());
    }
}
