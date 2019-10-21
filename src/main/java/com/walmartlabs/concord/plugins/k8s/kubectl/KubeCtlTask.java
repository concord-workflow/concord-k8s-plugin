package com.walmartlabs.concord.plugins.k8s.kubectl;

import com.walmartlabs.concord.plugins.tool.ToolCommand;
import com.walmartlabs.concord.plugins.tool.ToolInitializer;
import com.walmartlabs.concord.plugins.tool.ToolTaskSupport;
import com.walmartlabs.concord.sdk.InjectVariable;
import com.walmartlabs.concord.sdk.LockService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named("kubectl")
public class KubeCtlTask extends ToolTaskSupport {

    @InjectVariable("defaults")
    private Map<String, Object> defaults;

    @Inject
    public KubeCtlTask(Map<String, ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
        super(commands, lockService, toolInitializer);
    }
}
