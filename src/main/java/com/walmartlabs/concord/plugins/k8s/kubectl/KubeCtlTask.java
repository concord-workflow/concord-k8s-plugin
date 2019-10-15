package com.walmartlabs.concord.plugins.k8s.kubectl;


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
public class KubeCtlTask extends ToolTaskSupport {

  public static final String DEFAULT_VERSION = "v1.16.0";
  public static final String URL_TEMPLATE = "https://storage.googleapis.com/kubernetes-release/release/{version}/bin/{os}/{arch}/kubectl";

  @InjectVariable("defaults")
  private Map<String, Object> defaults;

  @Inject
  public KubeCtlTask(Map<String,ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
    super(commands, lockService, toolInitializer);
  }

  @Override
  public ToolDescriptor toolDescriptor() {
    return ImmutableToolDescriptor.builder()
        .id("kubectl")
        .name("KubeCtl")
        .executable("kubectl")
        .architecture("amd64")
        .namingStyle(NamingStyle.LOWER)
        .packaging(Packaging.FILE)
        .defaultVersion(DEFAULT_VERSION)
        .urlTemplate(URL_TEMPLATE)
        .build();
  }
}
