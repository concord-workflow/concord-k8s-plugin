package ca.vanzyl.concord.plugins.k8s.secrets;

import com.walmartlabs.concord.plugins.ConcordTestSupport;
import org.junit.Test;

import java.util.List;

import static io.airlift.security.pem.PemReader.isPem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecretsTest extends ConcordTestSupport
{

    @Test
    public void validateSecrets() throws Exception {

        SecretsManager secretsManager = new SecretsManager();
        List<Secret> secrets = secretsManager.load(file("secrets/secrets.yml"));

        Secret secret0 = secrets.get(0);
        assertThat(secret0.name()).isEqualTo("adminUsername");
        assertThat(secret0.value()).isEqualTo("admin");
        assertThat(secret0.description()).isEqualTo("Username for the administrative user on the cluster.");

        Secret secret1 = secrets.get(1);
        assertThat(secret1.name()).isEqualTo("adminPassword");
        assertThat(secret1.value()).isEqualTo("password");
        assertThat(secret1.description()).isEqualTo("Password for the administrative user on the cluster.");

        Secret secret2 = secrets.get(2);
        assertThat(secret2.name()).isEqualTo("fluentbitCertificate");
        assertThat(secret2.value()).contains("-----BEGIN CERTIFICATE-----");
        assertThat(secret2.value()).contains("-----END ENCRYPTED PRIVATE KEY-----");
        assertThat(secret2.description()).isEqualTo("Fluentbit Certificate.");

        String pemData = secret2.value();
        assertThat(isPem(pemData)).isTrue();
    }
}
