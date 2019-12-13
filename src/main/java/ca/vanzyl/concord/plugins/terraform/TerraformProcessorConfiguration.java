package ca.vanzyl.concord.plugins.terraform;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

//
//  - task: terraformProcessor
//    in:
//      version: 0.12
//      directory: terraform
//      region: us-west-2
//      authentication: credentials | assumeRole
//      resources:
//        - type: s3
//          configuration:
//            bucketName: tfState
//        - type: dynamodb
//          configuration:
//            tableName: tfState
//
@Value.Immutable
@JsonDeserialize(as = ImmutableTerraformProcessorConfiguration.class)
public abstract class TerraformProcessorConfiguration {

    @Nullable
    public abstract Boolean debug();

    public abstract String version();

    public abstract String provider();

    public abstract Map<String, Object> configuration();

    public abstract String authentication();

    public abstract List<TerraformResource> resources();
}
