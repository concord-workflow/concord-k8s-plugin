package ca.vanzyl.concord.plugins.k8s.helm3.commands;

import ca.vanzyl.concord.plugins.k8s.helm3.config.Chart;
import ca.vanzyl.concord.plugins.tool.ToolCommandSupport;
import ca.vanzyl.concord.plugins.tool.annotations.Flag;
import ca.vanzyl.concord.plugins.tool.annotations.Omit;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.sdk.Context;

import javax.inject.Named;
import java.io.File;
import java.nio.file.Path;

@Named("helm3/install")
public class Install extends ToolCommandSupport {

    @JsonProperty("chart")
    @Omit
    private Chart chart;

    public Chart chart() {
        return chart;
    }


    @JsonProperty("atomic")
    @Flag(name = {"--atomic"})
    private boolean atomic = true;

    @JsonProperty("create-namespace")
    @Flag(name = {"--create-namespace"})
    private boolean createNamespace = true;

    @Override
    public String idempotencyCheckCommand(Context context) {
        return String.format("{{executable}} status %s", chart.name());
    }

    @Override
    public void preProcess(Path workDir, Context context) {

        if (chart.values() != null) {
            interpolateWorkspaceFileAgainstContext(new File(chart.values()), context);
        }

        //
        // Patrick uses this to modify the version in the Chart.yml when deploying new versions
        //
        // Here is where we want to alter what Helm install is doing. If there is an externals configuration we want
        // fetch the Helm chart, insert the externals into the Helm chart and then install from the directory we
        // created with the fetched Helm chart
        //
        // - do any preparation work here and run any commands necessary. i need the path to the executable and access
        //   to the command and its configuration
        // - change the command line arguments as necessary. in the case of Helm we need to install from the directory
        //   just created.
        //
        /*
        if(chart.externals() != null) {
            //
            // helm fetch --version 1.7.4 --untar --untardir jenkins stable/jenkins
            //
            Path untardir = workDir(context).resolve("chart-" + chart.name());
            //
            // 1 = version
            // 2 = untardir
            // 3 = chart
            //
            String.format("{{executable}} fetch --version %s --untar --untardir %s %s", chart.version(), chart.name(), chart.value());
        }
         */
    }
}
