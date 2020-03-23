package ca.vanzyl.concord.plugins.k8s.kubectl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import ca.vanzyl.concord.plugins.tool.annotations.Option;
import ca.vanzyl.concord.plugins.tool.annotations.OptionWithEquals;
import ca.vanzyl.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Named;
import java.io.File;
import java.nio.file.Path;

@Named("kubectl/apply")
public class Apply extends ToolCommandSupport {

    @JsonProperty
    @OptionWithEquals(name = {"--validate"})
    private Boolean validate;

    @JsonProperty
    @Option(name = {"-f"})
    private String file;

    public Boolean validate() {
        return validate;
    }

    public String file() {
        return file;
    }

    @Override
    public void preProcess(Path workDir, Context context) throws Exception {
        // Interpolate the manifest to be applied before it is applied
        if (file != null) {
            interpolateWorkspaceFileAgainstContext(new File(file), context);
        }
    }
}
