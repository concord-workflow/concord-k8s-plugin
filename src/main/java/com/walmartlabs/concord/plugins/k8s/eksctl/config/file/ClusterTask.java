package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fireeye.k8s.ClusterGenerationRequest;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.sdk.Constants;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.ProjectInfo;
import com.walmartlabs.concord.sdk.SecretService;
import com.walmartlabs.concord.sdk.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.Map;

// This is in a K8s tree but this is strictly for interacting with Concord's secrets store.

@Named("cluster")
public class ClusterTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(ClusterTask.class);

    private final SecretService secretService;

    @Inject
    public ClusterTask(SecretService secretService) {
        this.secretService = secretService;
    }

    @Override
    public void execute(Context context) throws Exception {

        //
        // - task: cluster
        //   in:
        //     clusterRequest: ajay-003-dev-us-east-2.yml
        //     terraformfVars: "${terraformDirectory}/${clusterName}-${environment}-${region}.json"
        //
        String clusterRequest = (String) context.getVariable("clusterRequest");
        if (clusterRequest == null) {
            throw new Exception("The flow cannot continue with ${clusterRequestYml} being defined. Check your run.sh script.");
        }
        String terraformDirectory = (String) context.getVariable("terraformDirectory");
        String instanceId = (String) context.getVariable(Constants.Context.TX_ID_KEY);
        ProjectInfo projectInfo = (ProjectInfo) context.getVariable("projectInfo");
        String orgName = projectInfo.orgName();

        File requestFile = new File(clusterRequest);
        File terraformVarsFile = new File(terraformDirectory, "terraform.tfvars.json");
        ClusterGenerationRequestProcessor processor = new ClusterGenerationRequestProcessor();
        ClusterGenerationRequest request = processor.process(requestFile, terraformVarsFile);

        String clusterName = request.getCluster().getName();
        String region = request.getCluster().getRegion();
        String environment = request.getCluster().getEnvironment();
        String clusterId = String.format("%s-%s-%s", clusterName, environment, region);
        String kubeconfigName = "kubeconfig-" + clusterId;
        String kubeconfigFile = "/home/concord/.kube/kubeconfig-" + clusterId;

        context.setVariable("clusterName", clusterName);
        context.setVariable("region", region);
        context.setVariable("environment", environment);
        context.setVariable("clusterId", clusterId);
        // kubeconfig-${clusterName}-${environment}-${region}
        context.setVariable("kubeconfigName", kubeconfigName);
        // /home/concord/.kube/kubeconfig-${clusterId}
        context.setVariable("kubeconfigFile", kubeconfigFile);

        // Create envars for the execution of all the k8s-related tools
        Map<String,String> envars = Maps.newHashMap();
        envars.put("KUBECONFIG", kubeconfigFile);
        envars.put("AWS_ACCESS_KEY_ID", secretService.exportAsString(context, instanceId, orgName, "aws-access-key-id", null));
        envars.put("AWS_SECRET_ACCESS_KEY", secretService.exportAsString(context, instanceId, orgName, "aws-secret-access-key", null));
        envars.put("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/home/concord/bin");
        // Place the envars in the context for use by the k8s-related tools
        context.setVariable("envars", envars);
    }
}
