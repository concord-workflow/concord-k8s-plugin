package com.walmartlabs.concord.plugins.k8s.eksctl;


import com.walmartlabs.concord.plugins.tool.ToolCommand;
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

  private static final Logger logger = LoggerFactory.getLogger(EksCtlTask.class);

  @InjectVariable("defaults")
  private Map<String, Object> defaults;

  @Inject
  public EksCtlTask(Map<String, ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
    super(commands, lockService, toolInitializer);
  }
}
