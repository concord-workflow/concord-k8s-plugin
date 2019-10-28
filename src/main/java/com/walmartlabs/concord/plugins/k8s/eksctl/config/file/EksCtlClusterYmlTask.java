package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fireeye.k8s.ClusterGenerationRequest;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.SecretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;

@Named("eksctl-cluster-yml")
public class EksCtlClusterYmlTask extends ClusterTaskSupport {

    private static final Logger logger = LoggerFactory.getLogger(EksCtlClusterYmlTask.class);

    @Inject
    public EksCtlClusterYmlTask(SecretService secretService) {
        super(secretService);
    }

    @Override
    public void execute(Context context) throws Exception {

        // - task: eksctl-cluster-yml
        //   in:
        //     clusterRequest: "${clusterRequestYml}"
        //     vpcTerraformOutput: "${result.data}"

        String clusterRequest = (String) context.getVariable("clusterRequest");
        String vpcTerraformOutput = (String) context.getVariable("vpcTerraformOutput");
        ClusterGenerationRequest request = read(new File(clusterRequest));
        EksCtlYmlGenerator generator = new EksCtlYmlGenerator();

    }
}
