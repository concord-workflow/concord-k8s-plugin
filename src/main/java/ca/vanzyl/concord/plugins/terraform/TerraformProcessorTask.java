package ca.vanzyl.concord.plugins.terraform;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.walmartlabs.concord.plugins.Configurator;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Named;
import java.util.Map;

//
//    - task: terraformProcessor
//       in:
//         resourceDirectory: 00-aws/terraform
//         outputDirectory: terraform
//         configuration:
//           version: 0.12
//           provider: aws
//           authentication: credentials
//           configuration:
//             aws_region: us-east-2
//             aws_access_key: "${aws_access_key}"
//             aws_secret_key: "${aws_secret_key}"
//             tags:
//               creator: concord
//               session: jvz-001
//           resources:
//             - type: vpc
//               variables:
//                 vpc_name: jvz-vpc
//                 vpc_cidr: 10.206.0.0/18
//                 public_subnet_map:
//                   us-east-2a: 1
//                   us-east-2b: 2
//                   us-east-2c: 3
//                 private_subnet_map:
//                   us-east-2a: 4
//                   us-east-2b: 5
//                   us-east-2c: 6
//             - type: eks
//               variables:
//                 cluster_name: jvz-cluster
//
@Named("terraformProcessor")
public class TerraformProcessorTask extends TaskSupport {

    private final static String RESOURCE_DIRECTORY = "resourceDirectory";
    private final static String OUTPUT_DIRECTORY = "outputDirectory";
    private final static String CONFIGURATION = "configuration";

    private final Configurator configurator;

    public TerraformProcessorTask() {
        configurator = new Configurator();
    }

    @Override
    public void execute(Context context) throws Exception {

        String resources = (String) context.getVariable(RESOURCE_DIRECTORY);
        String outputDirectory = (String) context.getVariable(OUTPUT_DIRECTORY);

        TerraformProcessor processor = new TerraformProcessor(
                workDir(context).resolve(resources), workDir(context).resolve(outputDirectory));

        Map<String,Object> configuration = (Map<String,Object>)context.getVariable(CONFIGURATION);

        TerraformProcessingResult result = processor.process(configuration);
    }

}
