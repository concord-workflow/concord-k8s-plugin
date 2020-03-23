package ca.vanzyl.concord.plugins.tool;

import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public abstract class ToolCommandSupport implements ToolCommand {

    private static final Logger logger = LoggerFactory.getLogger(ToolCommandSupport.class);


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

    protected Map<String, Object> clusterRequest(Context context) {
        return (Map<String, Object>) context.getVariable("clusterRequest");
    }

    protected void interpolateWorkspaceFileAgainstContext(File file, Context context) {
        try {
            //
            // We need to take the values.yml that is provided and interpolate the content with the
            // Concord context. This allows passing in just-in-time configuration values derived from
            // any Concord operations and also allows passing in secret material from the Concord
            // secrets store or other secrets mechanisms the user may be using.
            //
            if (file.exists()) {
                String fileContent = new String(Files.readAllBytes(file.toPath()));
                if (fileContent.contains("${")) {
                    //
                    // We have interpolation work to do so we will backup the original file to another location
                    // and then created a new interpolated version of the values.yaml in the original location.
                    //
                    File fileOriginal = new File(file + ".original");
                    Files.copy(file.toPath(), fileOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    String interpolatedFileContent = (String) context.interpolate(fileContent);
                    Files.write(file.toPath(), interpolatedFileContent.getBytes());
                    logger.info("The {} file was interpolated to the following: \n\n{}", file.getName(), interpolatedFileContent);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
