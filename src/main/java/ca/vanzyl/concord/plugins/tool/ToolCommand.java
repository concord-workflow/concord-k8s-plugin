package ca.vanzyl.concord.plugins.tool;

import com.walmartlabs.concord.sdk.Context;

import java.nio.file.Path;

public interface ToolCommand {

    String idempotencyCheckCommand(Context context);

    int expectedIdempotencyCheckReturnValue();

    void preProcess(Path workDir, Context context) throws Exception;

    void postProcess(Path workDir, Context context) throws Exception;
}