package com.walmartlabs.concord.plugins.tool;

import com.walmartlabs.concord.sdk.Context;

import java.nio.file.Path;

public class ToolCommandSupport implements ToolCommand {

    @Override
    public String idempotencyCheckCommand() {
        return null;
    }

    @Override
    public void postProcess(Path workDir, Context context) throws Exception {}
}
