package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

//
// - task: eksctlYml
//   in:
//     clusterRequest: "${clusterRequestYml}"
//     vpcTerraformOutput: "${result.data}"
//
@Named("eksctlYml")
public class EksCtlClusterYmlTask extends TaskSupport {

    private static final Logger logger = LoggerFactory.getLogger(EksCtlClusterYmlTask.class);

    @Override
    public void execute(Context context) throws Exception {

        Map<String, Object> clusterRequest = (Map<String, Object>) context.getVariable("clusterRequest");
        Map<String, Object> terraformOutputAsMap = (Map<String, Object>) context.getVariable("vpcTerraformOutput");

        String clusterName = varAsString(clusterRequest, "clusterName");
        String region = varAsString(clusterRequest, "region");
        String k8sVersion = varAsString(clusterRequest, "k8sVersion");
        String user = varAsString(clusterRequest, "user");
        EksCtlYamlData clusterInfo = new EksCtlYamlData(clusterName, region, user, k8sVersion, terraformOutputAsMap);

        String eksctlYamlFile = varAsString(clusterRequest, "eksctlYamlFile");
        Path clusterYml = workDir(context).resolve(eksctlYamlFile);
        EksCtlYamlGenerator generator = new EksCtlYamlGenerator();
        generator.generate(clusterInfo, clusterYml.toFile());

        String clusterYmlContent = new String(Files.readAllBytes(clusterYml));
        logger.info("cluster.yml:\n\n{}", clusterYmlContent);
    }
}
