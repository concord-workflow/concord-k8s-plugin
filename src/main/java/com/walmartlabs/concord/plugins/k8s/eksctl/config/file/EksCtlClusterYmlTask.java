package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fireeye.k8s.ClusterGenerationRequest;
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

@Named("eksctlYml")
public class EksCtlClusterYmlTask extends ClusterTaskSupport {

    private static final Logger logger = LoggerFactory.getLogger(EksCtlClusterYmlTask.class);

    @Inject
    public EksCtlClusterYmlTask(SecretService secretService) {
        super(secretService);
    }

    @Override
    public void execute(Context context) throws Exception {

        // - task: eksctlYml
        //   in:
        //     clusterRequest: "${clusterRequestYml}"
        //     vpcTerraformOutput: "${result.data}"

        String clusterRequest = (String) context.getVariable("clusterRequest");

        Map<String,Object> terraformOutputAsMap = (Map<String,Object>) context.getVariable("vpcTerraformOutput");

        //String vpcTerraformOutput = (String) context.getVariable("vpcTerraformOutput");
        ClusterGenerationRequest request = read(new File(clusterRequest));

        Path workDir = workDir(context);

        EksCtlYmlGenerator generator = new EksCtlYmlGenerator();
        // Generate the cluster.yml for EksCtl and the autoscaler.yml in the workdirectory
        generator.generate(request, terraformOutputAsMap, workDir.toFile());

        // Display the cluster.yml
        Path clusterYml = workDir.resolve("cluster.yml");
        String clusterYmlContent = new String(Files.readAllBytes(clusterYml));
        logger.info("cluster.yml:\n" + clusterYmlContent);

    }
}
