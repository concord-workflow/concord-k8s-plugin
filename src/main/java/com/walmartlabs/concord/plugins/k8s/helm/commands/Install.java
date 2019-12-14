package com.walmartlabs.concord.plugins.k8s.helm.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmartlabs.concord.plugins.k8s.helm.config.Chart;
import com.walmartlabs.concord.plugins.tool.Omit;
import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.sdk.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Named("helm/install")
public class Install extends ToolCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(Install.class);

    @JsonProperty("chart")
    @Omit
    private Chart chart;
    public Chart chart() {
        return chart;
    }

    @JsonProperty("repo")
    @Omit
    private String repo;
    public String repo() { return repo; }

    @JsonProperty("pre")
    @Omit
    private List<String> pre;
    public List<String> pre() { return pre; }

    @JsonProperty("post")
    @Omit
    private List<String> post;
    public List<String> post() { return post; }

    @Override
    public String idempotencyCheckCommand(Context context) {
        return String.format("{{executable}} status %s", chart.name());
    }

    @Override
    public void preProcess(Path workDir, Context context) {

        if(chart.values() != null) {
            try {
                //
                // We need to take the values.yml that is provided and interpolate the content with the
                // Concord context. This allows passing in just-in-time configuration values derived from
                // any Concord operations and also allows passing in secret material from the Concord
                // secrets store or other secrets mechanisms the user may be using.
                //
                File valuesYamlFile = new File(chart.values());
                if (valuesYamlFile.exists()) {
                    String valuesYamlContent = new String(Files.readAllBytes(valuesYamlFile.toPath()));
                    if (valuesYamlContent.contains("${")) {
                        //
                        // We have interpolation work to do so we will backup the original file to another location
                        // and then created a new interpolated version of the values.yaml in the original location.
                        //
                        File valuesYamlFileOriginal = new File(chart.values() + ".original");
                        Files.copy(valuesYamlFile.toPath(), valuesYamlFileOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        String interpolatedValuesYamlContent = (String) context.interpolate(valuesYamlContent);
                        Files.write(valuesYamlFile.toPath(), interpolatedValuesYamlContent.getBytes());

                        logger.info("The {} file was interpolated to the following: \n\n{}", chart.values(), interpolatedValuesYamlContent);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        //
        // Patrick uses this to modify the version in the Chart.yml when deploying new versions
        // e
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
