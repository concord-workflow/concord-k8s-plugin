package ca.vanzyl.concord.plugins;

import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.SecretService;
import com.walmartlabs.concord.sdk.Task;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public abstract class TaskSupport implements Task {

    protected Path workDir(Context context, String path) {
        return workDir(context).resolve(path);
    }

    protected Path workDir(Context context) {
        Path workDir = Paths.get((String) context.getVariable(com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY));
        if (workDir == null) {
            throw new IllegalArgumentException("Can't determine the current '" + com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY + "'");
        }
        return workDir;
    }

    protected String varAsString(Context context, String variableName) {
        return (String) context.getVariable(variableName);
    }

    protected String varAsString(Map<String,Object> context, String variableName) {
        return (String) context.get(variableName);
    }

}
