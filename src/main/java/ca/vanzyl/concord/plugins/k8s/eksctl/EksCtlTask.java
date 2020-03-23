package ca.vanzyl.concord.plugins.k8s.eksctl;

import ca.vanzyl.concord.plugins.tool.ToolCommand;
import ca.vanzyl.concord.plugins.tool.ToolInitializer;
import ca.vanzyl.concord.plugins.tool.ToolTaskSupport;
import com.walmartlabs.concord.sdk.InjectVariable;
import com.walmartlabs.concord.sdk.LockService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named("eksctl")
public class EksCtlTask extends ToolTaskSupport {

    @InjectVariable("defaults")
    private Map<String, Object> defaults;

    @Inject
    public EksCtlTask(Map<String, ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
        super(commands, lockService, toolInitializer);
    }
}
