package com.walmartlabs.concord.plugins.tool;

import com.walmartlabs.concord.sdk.Context;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public abstract class ToolCommandSupport implements ToolCommand {

    @Override
    public String idempotencyCheckCommand(Context context) {
        return null;
    }

    @Override
    public int expectedIdempotencyCheckReturnValue() {
        return 0;
    }

    @Override
    public void preProcess(Path workDir, Context context) throws Exception {
    }

    @Override
    public void postProcess(Path workDir, Context context) throws Exception {
    }

    protected Path workDir(Context context) {
        Path workDir = Paths.get((String) context.getVariable(com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY));
        if (workDir == null) {
            throw new IllegalArgumentException("Can't determine the current '" + com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY + "'");
        }
        return workDir;
    }

    protected String clusterRequestVarAsString(Context context, String variable) {
        return (String) clusterRequest(context).get(variable);
    }

    protected Map<String,Object> clusterRequest(Context context) {
        return (Map<String,Object>) context.getVariable("clusterRequest");
    }

    protected String varAsString(Context context, String name) {
        return (String) context.getVariable(name);
    }
}
