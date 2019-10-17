package com.walmartlabs.concord.plugins.k8s.eksctl;

import com.fireeye.k8s.ClusterGenerationRequest;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.file.ClusterGenerationRequestProcessor;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.file.ClusterInfo;
import com.walmartlabs.concord.plugins.k8s.eksctl.config.file.EksCtlClusterYmlGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//
// Terraform preparation
// java -jar k8s-tools.jar cluster-003.yml
//
// Etksctl preparation
// java -jar k8s-tools.jar <cluster-yaml-file>
//

public class Main {

    public static void main(String[] args) throws IOException {

        if(args.length != 2) {
            System.err.println("java -jar k8s-tools.jar <tf | eksctl> <cluster-request-yml");
            System.exit(1);
        }

        if(args[0].equalsIgnoreCase("tf")) {

            ClusterGenerationRequestProcessor processor = new ClusterGenerationRequestProcessor();
            processor.process(new File(args[1]));

        } else if(args[0].equalsIgnoreCase("eksctl")) {

            File requestFile = new File(args[1]);
            ClusterGenerationRequestProcessor processor = new ClusterGenerationRequestProcessor();
            ClusterGenerationRequest request = processor.process(requestFile);

            EksCtlClusterYmlGenerator generator = new EksCtlClusterYmlGenerator(request);
            File tfOutput = new File(requestFile.getParentFile(), request.getCluster().getName() + "-tf-output.json");
            System.out.println("TF output JSON: " + tfOutput);
            ClusterInfo cluster = generator.parse(tfOutput);

            File eksCtlYmlFile = new File(requestFile.getParentFile(), request.getCluster().getName() + "-eksctl.yml");
            System.out.println("EksCtl YAML: " + eksCtlYmlFile);
            generator.clusterYml(cluster, new FileOutputStream(eksCtlYmlFile));
            File autoscalerFile = new File(requestFile.getParentFile(), request.getCluster().getName() + "-autoscaler.yml");
            System.out.println("Eks Autoscaler YAML: " + autoscalerFile);
            generator.autoscalerYml(cluster, new FileOutputStream(autoscalerFile));

        } else {

            System.err.println("The command %s is not a valid command. Supported commands are 'tf' and 'eksctl'.");

        }
    }
}
