package ca.vanzyl.concord.plugins.shell;

import ca.vanzyl.concord.plugins.tool.ToolCommand;
import ca.vanzyl.concord.plugins.tool.ToolInitializer;
import ca.vanzyl.concord.plugins.tool.ToolTaskSupport;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.LockService;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Named("download")
public class ShellTask extends ToolTaskSupport {

    @Inject
    public ShellTask(Map<String, ToolCommand> commands, LockService lockService, ToolInitializer toolInitializer) {
        super(commands, lockService, toolInitializer);
    }

    @Override
    public void execute(Context context) throws Exception {

        Path workDir = Paths.get((String) context.getVariable(com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY));
        if (workDir == null) {
            throw new IllegalArgumentException("Can't determine the current '" + com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY + "'");
        }

        String script = (String) context.getVariable("script");
    }
}
