package ca.vanzyl.concord.secrets;

import ca.vanzyl.concord.secrets.aws.AsmClient;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class AsmClientTest {

    @Test
    @Ignore
    public void validateAsmClientReturnsNullOnMissingSecret() {
        AsmClient client = new AsmClient("us-west-2");
        String kubeconfigName = "this-secret-does-not-exist";
        String kubeconfigContent = client.get(kubeconfigName);
        assertThat(kubeconfigContent).isNull();
    }
}
