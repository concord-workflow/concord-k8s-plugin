package ca.vanzyl.concord.plugins.k8s.secrets;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

/**
 * Client for creating secrets in a K8s cluster
 */
public class K8sSecretsClient {

    private final DefaultKubernetesClient client;

    public K8sSecretsClient(String clusterName) throws Exception {
        // TODO: this is just our convention in the way we're storing kubeconfig files
        this(new File(System.getProperty("user.home"), ".kube/kubeconfig-" + clusterName));
    }

    public K8sSecretsClient(File kubeconfig) throws Exception {
        String kubeconfigContent = new String(Files.readAllBytes(kubeconfig.toPath()));
        kubeconfigContent = modifyKubeconfigContent(kubeconfigContent);
        this.client = new DefaultKubernetesClient(Config.fromKubeconfig(kubeconfigContent));
    }

    public String modifyKubeconfigContent(String kubeconfigContent) {
        return kubeconfigContent.replace("command: aws-iam-authenticator", "command: /home/concord/bin/aws-iam-authenticator");
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
