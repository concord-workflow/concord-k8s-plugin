package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fireeye.k8s.ClusterGenerationRequest;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.sdk.Constants;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.SecretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

// This is in a K8s tree but this is strictly for interacting with Concord's secrets store.

@Named("cluster")
public class ClusterTask extends ClusterTaskSupport {

    private static final Logger logger = LoggerFactory.getLogger(ClusterTask.class);

    @Inject
    public ClusterTask(SecretService secretService) {
        super(secretService);
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

        String terraformDirectoryIdentifier = (String) context.getVariable("terraformDirectory");
        Path workDir = workDir(context);
        File terraformDirectory = workDir.resolve(terraformDirectoryIdentifier).toFile();

        String instanceId = (String) context.getVariable(Constants.Context.TX_ID_KEY);
        Map<String, Object> projectInfo = (Map<String, Object>) context.getVariable("projectInfo");
        String orgName = (String) projectInfo.get("orgName");

        File requestFile = new File(clusterRequest);
        File terraformVarsFile = new File(terraformDirectory, "cluster.auto.tfvars.json");
        ClusterGenerationRequestProcessor processor = new ClusterGenerationRequestProcessor();
        ClusterGenerationRequest request = processor.process(requestFile, terraformVarsFile);

        // Display the content of the terraform vars file for debugging purposes
        logger.info("Terraform TF Vars written to " + terraformVarsFile);
        String terraformVarsFileContent = new String(Files.readAllBytes(terraformVarsFile.toPath()));
        logger.info("terraformVarsFileContent:\n" + terraformVarsFileContent);

        String clusterName = request.getCluster().getName();
        String region = request.getCluster().getRegion();
        String environment = request.getCluster().getEnvironment();
        String clusterId = String.format("%s-%s-%s", clusterName, environment, region);
        String kubeconfigName = "kubeconfig-" + clusterId;
        String kubeconfigFile = "/home/concord/.kube/kubeconfig-" + clusterId;

        logger.info("Variables we have created for use in Concord:");
        logger.info("clusterName: {}", clusterName);
        logger.info("region: {}", region);
        logger.info("environment: {}", environment);
        logger.info("clusterId: {}", clusterId);
        logger.info("kubeconfigName: {}", kubeconfigName);
        logger.info("kubeconfigFile: {}", kubeconfigFile);

        context.setVariable("clusterName", clusterName);
        context.setVariable("region", region);
        context.setVariable("environment", environment);
        context.setVariable("clusterId", clusterId);
        // kubeconfig-${clusterName}-${environment}-${region}
        context.setVariable("kubeconfigName", kubeconfigName);
        // /home/concord/.kube/kubeconfig-${clusterId}
        context.setVariable("kubeconfigFile", kubeconfigFile);

        // Create envars for the execution of all the k8s-related tools
        Map<String, String> envars = Maps.newHashMap();
        envars.put("KUBECONFIG", kubeconfigFile);
        envars.put("AWS_ACCESS_KEY_ID", secretService.exportAsString(context, instanceId, orgName, "aws-access-key-id", null));
        envars.put("AWS_SECRET_ACCESS_KEY", secretService.exportAsString(context, instanceId, orgName, "aws-secret-access-key", null));
        String path = "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/home/concord/bin";
        envars.put("PATH", path);
        // Place the envars in the context for use by the k8s-related tools
        context.setVariable("envars", envars);

        logger.info("ENVARS we have made available to tasks:");
        logger.info("KUBECONFIG: {}", kubeconfigFile);
        logger.info("AWS_ACCESS_KEY_ID: {}", "XXX");
        logger.info("AWS_SECRET_ACCESS_KEY: {}", "XXX");
        logger.info("PATH: {}", path);
    }
}
