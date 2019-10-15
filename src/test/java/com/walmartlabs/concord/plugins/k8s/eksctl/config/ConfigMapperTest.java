package com.walmartlabs.concord.plugins.k8s.eksctl.config;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.walmartlabs.concord.plugins.tool.ToolConfigurationMapper;
import java.util.Map;
import org.junit.Test;

public class ConfigMapperTest {

  @Test
  public void validateConfigMapper() throws Exception {

    Map<String, Object> input = mapBuilder()
        .put("command", "create")
        .put("cluster",
            mapBuilder().put("configFile", "cluster.yaml")
                .build())
        .build();

    Create create = new Create();

    new ToolConfigurationMapper().configureCommand(input, create);

    assertEquals("cluster.yaml", create.cluster().configFile());
  }

  public static Builder<String, Object> mapBuilder() {
    return ImmutableMap.builder();
  }
}
