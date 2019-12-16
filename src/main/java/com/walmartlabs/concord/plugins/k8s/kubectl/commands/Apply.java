package com.walmartlabs.concord.plugins.k8s.kubectl.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.tool.OptionWithEquals;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.sdk.Context;
import io.airlift.airline.Option;

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
