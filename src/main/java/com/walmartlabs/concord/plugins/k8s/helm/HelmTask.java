package com.walmartlabs.concord.plugins.k8s.helm;


import com.walmartlabs.concord.plugins.tool.ImmutableToolDescriptor;
import com.walmartlabs.concord.plugins.tool.ToolCommand;
import com.walmartlabs.concord.plugins.tool.ToolTaskSupport;
import com.walmartlabs.concord.plugins.tool.ToolDescriptor;
import com.walmartlabs.concord.plugins.tool.ToolDescriptor.NamingStyle;
import com.walmartlabs.concord.plugins.tool.ToolDescriptor.Packaging;
import com.walmartlabs.concord.plugins.tool.ToolInitializer;
import com.walmartlabs.concord.sdk.InjectVariable;
import com.walmartlabs.concord.sdk.LockService;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

@Named("helm")
public class HelmTask extends ToolTaskSupport {

  public static final String DEFAULT_VERSION = "v2.14.3";
  public static final String URL_TEMPLATE = "https://get.helm.sh/helm-{version}-{os}-{arch}.tar.gz";

  @InjectVariable("defaults")
  private Map<String, Object> defaults;

  @Inject
  public HelmTask(Map<String,ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
    super(commands, lockService, toolInitializer);
  }

  @Override
  public ToolDescriptor toolDescriptor() {
    return ImmutableToolDescriptor.builder()
        .id("helm")
        .name("Helm")
        .executable("helm")
        .architecture("amd64")
        .namingStyle(NamingStyle.LOWER)
        .packaging(Packaging.TARGZ)
        .defaultVersion(DEFAULT_VERSION)
        .urlTemplate(URL_TEMPLATE)
        .build();
  }

  @Override
  public Class<?> configurationClass() {
    return null;
  }
}

