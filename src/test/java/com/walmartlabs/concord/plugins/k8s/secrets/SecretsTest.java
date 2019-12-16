package com.walmartlabs.concord.plugins.k8s.secrets;

import com.walmartlabs.concord.plugins.TestSupport;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecretsTest extends TestSupport {

    @Test
    public void validateSecrets() throws Exception {

        SecretsManager secretsManager = new SecretsManager();
        List<Secret> secrets = secretsManager.load(file("secrets/secrets.yml"));

        Secret secret0 = secrets.get(0);
        assertEquals("adminUsername", secret0.name());
        assertEquals("admin", secret0.value());
        assertEquals("Username for the administrative user on the cluster.", secret0.description());

        Secret secret1 = secrets.get(1);
        assertEquals("adminPassword", secret1.name());
        assertEquals("password", secret1.value());
        assertEquals("Password for the administrative user on the cluster.", secret1.description());

        Secret secret2 = secrets.get(2);
        assertEquals("fluentbitCertificate", secret2.name());
        assertTrue(secret2.value().contains("-----BEGIN CERTIFICATE-----"));
        assertTrue(secret2.value().contains("-----END CERTIFICATE-----"));
        assertEquals("Fluentbit Certificate.", secret2.description());
    }
}
