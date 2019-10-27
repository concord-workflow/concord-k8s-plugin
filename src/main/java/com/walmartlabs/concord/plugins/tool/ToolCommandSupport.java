package com.walmartlabs.concord.plugins.tool;

import com.walmartlabs.concord.sdk.Context;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ToolCommandSupport implements ToolCommand {

    @Override
    public String idempotencyCheckCommand() {
        return null;
    }

    @Override
    public void postProcess(Path workDir, Context context) throws Exception {}

    protected Path workDir(Context context) {
        Path workDir = Paths.get((String) context.getVariable(com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY));
        if (workDir == null) {
            throw new IllegalArgumentException("Can't determine the current '" + com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY + "'");
        }
        return workDir;
    }
}
