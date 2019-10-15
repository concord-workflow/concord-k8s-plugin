package com.walmartlabs.concord.plugins.k8s.eksctl.config;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.walmartlabs.concord.plugins.tool.ToolConfigurationMapper;
import java.util.Map;
import org.junit.Test;

public class ConfigMapperTest {

  @Test
  public void validateConfigMapper() {

    Map<String, Object> input = mapBuilder()
        .put("create",
            mapBuilder().put("cluster",
                mapBuilder().put("configFile", "cluster.yaml")
                    .build())
                .build())
        .build();

    EksCtlConfiguration config = new ToolConfigurationMapper().map(input, EksCtlConfiguration.class);

    assertEquals("cluster.yaml", config.create().cluster().configFile());
  }

  public static Builder<String, Object> mapBuilder() {
    return ImmutableMap.builder();
  }
}
