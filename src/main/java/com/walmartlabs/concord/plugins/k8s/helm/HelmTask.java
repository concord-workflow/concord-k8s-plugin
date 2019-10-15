package com.walmartlabs.concord.plugins.k8s.helm;

import com.walmartlabs.concord.plugins.tool.ToolCommand;
import com.walmartlabs.concord.plugins.tool.ToolInitializer;
import com.walmartlabs.concord.plugins.tool.ToolTaskSupport;
import com.walmartlabs.concord.sdk.InjectVariable;
import com.walmartlabs.concord.sdk.LockService;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

@Named("helm")
public class HelmTask extends ToolTaskSupport {

  @InjectVariable("defaults")
  private Map<String, Object> defaults;

  @Inject
  public HelmTask(Map<String, ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
    super(commands, lockService, toolInitializer);
  }
}

