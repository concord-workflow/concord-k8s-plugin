package com.walmartlabs.concord.plugins.k8s.secrets;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

public class SecretsClient {

    private final DefaultKubernetesClient client;

    public SecretsClient(String clusterName) throws Exception {
        this(new File(System.getProperty("user.home"), ".kube/kubeconfig-" + clusterName));
    }

    public SecretsClient(File kubeconfig) throws Exception {
        this.client = new DefaultKubernetesClient(Config.fromKubeconfig(new String(Files.readAllBytes(kubeconfig.toPath()))));
    }

    public void addSecret(String namespace, String name, Map<String, String> data) {

        Secret secret = new SecretBuilder()
                .withNewMetadata()
                .withName(name)
                .addToLabels("com.walmartlabs.concord/secret", name)
                .endMetadata()
                .addToData(data)
                .build();

        //
        // kubectl create secret generic \
        //   test-secret \
        //   --from-literal=username='my-app' \
        //   --from-literal=password='39528$vdg7Jb'
        //

        client.secrets().inNamespace(namespace).create(secret);
    }
}
