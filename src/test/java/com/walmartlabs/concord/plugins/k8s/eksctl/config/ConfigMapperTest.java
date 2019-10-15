package com.walmartlabs.concord.plugins.k8s.eksctl.config;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.walmartlabs.concord.plugins.k8s.eksctl.commands.Create;
import com.walmartlabs.concord.plugins.tool.ToolConfigurator;
import java.util.Map;
import org.junit.Test;

public class ConfigMapperTest {

  @Test
  public void validateConfigMapper() throws Exception {

    Map<String, Object> input = mapBuilder()
        .put("command", "create")
        .put("cluster",
            mapBuilder()
                .put("configFile", "cluster.yaml")
                .put("kubeConfig", "/home/concord/.kube/config")
                .build())
        .build();

    Create create = new Create();

    new ToolConfigurator().configureCommand(input, create);

    assertEquals("cluster.yaml", create.cluster().configFile());
    assertEquals("/home/concord/.kube/config", create.cluster().kubeConfig());
  }

  public static Builder<String, Object> mapBuilder() {
    return ImmutableMap.builder();
  }
}
