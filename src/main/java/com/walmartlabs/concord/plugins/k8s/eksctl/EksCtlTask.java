package com.walmartlabs.concord.plugins.k8s.eksctl;


import com.walmartlabs.concord.plugins.tool.ImmutableToolDescriptor;
import com.walmartlabs.concord.plugins.tool.ToolCommand;
import com.walmartlabs.concord.plugins.tool.ToolDescriptor;
import com.walmartlabs.concord.plugins.tool.ToolDescriptor.NamingStyle;
import com.walmartlabs.concord.plugins.tool.ToolDescriptor.Packaging;
import com.walmartlabs.concord.plugins.tool.ToolInitializer;
import com.walmartlabs.concord.plugins.tool.ToolTaskSupport;
import com.walmartlabs.concord.sdk.InjectVariable;
import com.walmartlabs.concord.sdk.LockService;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("eksctl")
public class EksCtlTask extends ToolTaskSupport {

  public static final String DEFAULT_VERSION = "0.6.0";
  public static final String URL_TEMPLATE = "https://github.com/weaveworks/eksctl/releases/download/{version}/eksctl_{os}_{arch}.tar.gz";

  private static final Logger logger = LoggerFactory.getLogger(EksCtlTask.class);

  @InjectVariable("defaults")
  private Map<String, Object> defaults;

  @Inject
  public EksCtlTask(Map<String,ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
    super(commands, lockService, toolInitializer);
  }

  //
  // This can likely be a configuration stored as a resource as it's all static for the most part, you might layer in
  // a custom version or a custom url template
  //
  @Override
  public ToolDescriptor toolDescriptor() {
    return ImmutableToolDescriptor.builder()
        .id("eksctl")
        .name("EksCtl")
        .executable("eksctl")
        .architecture("amd64")
        .namingStyle(NamingStyle.CAPITALIZE)
        .packaging(Packaging.TARGZ)
        .defaultVersion(DEFAULT_VERSION)
        .urlTemplate(URL_TEMPLATE)
        .build();
  }
}
