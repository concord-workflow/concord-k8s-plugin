package ca.vanzyl.concord.plugins.k8s.secrets;

import com.walmartlabs.concord.client.ApiClientFactory;
import ca.vanzyl.concord.plugins.k8s.K8sTaskSupport;
import com.walmartlabs.concord.sdk.Constants;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.InjectVariable;
import com.walmartlabs.concord.sdk.SecretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
//
// This is a little cheat to help with bootstrap secrets. We want to store as many secrets as possible in a system
// like ASM. But, clearly, we can't store the credentials for AWS in AWS so we need a way to store these in Concord
// to bootstrap getting the rest of the secrets, but we want the user to be able to use the same references:
//
// ${secrets.awsAccessKey}
// ${secrets.awsSecretKey}
//
// ${secrets.adminUsername}
//
// The first two are bootstrap secrets, and the third comes from ASM
//
@Named("secrets")
public class BootstrapSecretsTask extends K8sTaskSupport implements Map<String,Object> {

    private final static Logger logger = LoggerFactory.getLogger(BootstrapSecretsTask.class);

    @InjectVariable(Constants.Context.CONTEXT_KEY)
    private Context context;

    @InjectVariable(Constants.Context.TX_ID_KEY)
    private String instanceId;

    private final SecretService secretService;

    @Inject
    public BootstrapSecretsTask(ApiClientFactory apiClientFactory, SecretService secretService) {
        super(apiClientFactory);
        this.secretService = secretService;
    }

    public String secret(String name) {
        String credentialsNamespace = clusterRequest(context, "credentialsNamespace");
        return secret(name, credentialsNamespace);
    }

    public String secret(String name, String credentialsNamespace) {
        String organization = orgName(context);
        String secretName = credentialsNamespace + "-" + name;
        try {
            String secret = secretService.exportAsString(context, instanceId, organization, secretName, null);
            Map<String,String> secrets = (Map<String,String>) context.getVariable("bootstrap");
            /*

            I cannot seem to populate this here, I get an IllegalStateException, but if I populate the
            concord.yml with:

            bootstrap: {}

            It works fine.

            if(secrets == null) {
                secrets = Maps.newHashMap();
                context.setVariable("bootstrapSecrets", secrets);
            }
             */
            secrets.put(name, secret);
            return secret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object get(Object key) {
        return secret((String)key);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object put(String key, Object value) {
        return null;
    }

    @Override
    public Object remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<String> keySet() {
        return null;
    }

    @Override
    public Collection<Object> values() {
        return null;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return null;
    }

    // This is all superfluous, I just want to be able to refer to secrets in
    // in the context as ${secrets.whatever}

}
