package ca.vanzyl.concord.plugins.k8s.initialize;

import ca.vanzyl.concord.plugins.k8s.K8sTaskSupport;
import ca.vanzyl.concord.secrets.aws.AsmKubeconfigGetTask;
import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;
import java.util.Map;

@Named("k8sValidate")
public class K8sValidateTask extends K8sTaskSupport {

    private final static Logger logger = LoggerFactory.getLogger(K8sValidateTask.class);

    @Inject
    public K8sValidateTask(ApiClientFactory apiClientFactory) {
        super(apiClientFactory);
    }

    @Override
    public void execute(Context context) throws Exception {

        // Completely validate all necessary secrets
        Object secrets = context.getVariable("secrets");
        if (secrets instanceof List) {
            logger.warn("Forcing coercion of secrets map!");
            context.setVariable("secrets", ((List)secrets).get(0));
        }
    }
}
