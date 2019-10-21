package com.walmartlabs.concord.plugins.tool;

import com.walmartlabs.concord.sdk.Context;

import java.nio.file.Path;

public interface ToolCommand {

    String idempotencyCheckCommand();

    void postProcess(Path workDir, Context context) throws Exception;
}