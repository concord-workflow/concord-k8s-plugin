package com.walmartlabs.concord.plugins.k8s.secrets;

import com.google.common.collect.ImmutableMap;

import java.util.Base64;

public class Main {

    public static void main(String[] args) throws Exception {

        SecretsClient client = new SecretsClient("ajay-003");
        client.addSecret("concord", "fluentbit2",
                ImmutableMap.of(
                        "fluentbitKey", base64("fluentbitKeyValue"),
                        "fluentbitCert", base64("fluentbitCertValue"),
                        "fluentbitSharedKey", base64("fluentbitSharedKeyValue")));
    }

    private static String base64(String originalInput) {
        return Base64.getEncoder().encodeToString(originalInput.getBytes());
    }
}
