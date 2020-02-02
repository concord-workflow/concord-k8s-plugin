package ca.vanzyl.concord.plugins.terraform;

import com.bertramlabs.plugins.hcl4j.HCLParser;
import com.bertramlabs.plugins.hcl4j.RuntimeSymbols.Variable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.walmartlabs.concord.plugins.Configurator;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static ca.vanzyl.concord.plugins.Utils.emptyDirectoryIfContentPresent;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

// TODO: need a better way to share common variables across resources types. For example the "vpn" and "vpc" resources both require "vpc_name"
// TODO: parameterize the name of the variables file that's produced
public class TerraformProcessor {

    private final static Logger logger = LoggerFactory.getLogger(TerraformProcessor.class);

    private final static String AUTHENTICATION_CREDENTIALS = "credentials";
    private final static String AUTHENTICATION_ASSUME_ROLE = "assume-role";
    // These are separate as https://github.com/bertramdev/hcl4j can't deal with TF 0.12+
    private final static String PROVIDER_RESOURCE = "00-provider-%s.tf";
    private final static String PROVIDER_VARIABLES = "00-provider-%s-variables.tf";

    private final static String NETWORK_VARIABLES = "00-network-variables.tf";

    private final Path resources;
    private final Path outputDirectory;
    private final Path workDir;
    private final Configurator configurator;

    public TerraformProcessor(Path resources, Path outputDirectory, Path workDir) {
        this.resources = resources;
        this.outputDirectory = outputDirectory;
        this.workDir = workDir;
        this.configurator = new Configurator(true);
    }

    public TerraformProcessingResult process(Map<String, Object> configuration) throws Exception {
        return process(configurator.createConfiguration(configuration, TerraformProcessorConfiguration.class));
    }

    public TerraformProcessingResult process(TerraformProcessorConfiguration configuration) throws Exception {

        if (!configuration.authentication().equals(AUTHENTICATION_CREDENTIALS) && !configuration.authentication().equals(AUTHENTICATION_ASSUME_ROLE)) {
            throw new RuntimeException(String.format("The authentication mode of '%s' is not supported. Supported authentication modes are 'credentials' and 'assumeRole'.", configuration.authentication()));
        }

        if(!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
        }

        //
        // It should be safe to delete this as we should be able to reproduce it exactly on each run. Changes would
        // be like manually editing the Terraform and running it again.
        //
        if (configuration.debug() == null || !configuration.debug()) {
            emptyDirectoryIfContentPresent(outputDirectory);
        }

        // /base/terraform/{version}/{provider}
        Path terraformResources = resources.resolve(configuration.version()).resolve(configuration.provider());

        // All the variables we are collecting to make our Terraform JSON variables file
        List<TerraformVariable> variables = Lists.newArrayList();

        //
        // This resource contains the provider definition:
        //
        // 00-provider-credentials.tf
        //
        String providerName = String.format(PROVIDER_RESOURCE, configuration.authentication());
        Path providerResource = terraformResources.resolve(providerName);
        Files.copy(providerResource, outputDirectory.resolve(providerName), REPLACE_EXISTING);
        //
        // This resource contains the variables declarations for the selected provider:
        //
        // 00-provider-credentials-variables.tf
        //
        String providerVariablesFileName = String.format(PROVIDER_VARIABLES, configuration.authentication());
        Path providerVariablesFile = terraformResources.resolve(providerVariablesFileName);
        Files.copy(providerVariablesFile, outputDirectory.resolve(providerVariablesFileName), REPLACE_EXISTING);

        // Variables for the provider need to be added
        variables.addAll(processVariables("provider", providerVariablesFile, configuration.configuration()));

        List<String> resourceTypes = Lists.newArrayList();
        for (TerraformResource resource : configuration.resources()) {
            resourceTypes.add(resource.type());
            Path source = terraformResources.resolve(resource.type());
            copy(source, outputDirectory);
            copyPoliciesToWorkDir(source, workDir);

            Path resourceVariablesFile = source.resolve(String.format("%s-variables.tf", resource.type()));
            variables.addAll(processVariables(resource.type(), resourceVariablesFile, resource.configuration()));

            // TODO: this needs to work better for shared variables. Maybe this is really only going to be for networking
            if (resource.type().equals("vpc") || resource.type().equals("vpn")) {
                Path networkVariables = terraformResources.resolve(NETWORK_VARIABLES);
                Files.copy(networkVariables, outputDirectory.resolve(NETWORK_VARIABLES), REPLACE_EXISTING);
                variables.addAll(processVariables("network", networkVariables, resource.configuration()));
            }
        }

        //
        // For all the variables that are required by Terraform we generate a JSON file that contains
        // the required values.
        //
        Map<String, Object> terraformVariablesJsonMap = Maps.newTreeMap();
        for (TerraformVariable variable : variables) {
            terraformVariablesJsonMap.put(variable.name, variable.value);
        }
        String terraformVariablesJson = new ObjectMapper()
                .writerWithDefaultPrettyPrinter().writeValueAsString(terraformVariablesJsonMap);

        //
        // Write out the variables JSON. Default modes are CREATE | TRUNCATE_EXISTING | WRITE
        //
        Path tfVars = outputDirectory.resolve("00.auto.tfvars.json");
        //logger.info("Writing JSON variables to {}:", tfVars);
        //logger.info("\n{}", terraformVariablesJson);
        Files.write(tfVars, terraformVariablesJson.getBytes());

        logger.info("Terraform resources written to {} based on resource requests:", outputDirectory);
        Files.list(outputDirectory)
                .sorted()
                .filter(Files::isRegularFile)
                .forEach(s -> logger.info("{}", s.getFileName()));

        //
        // The variables we are returning are the variables required to satisfy the Terraform
        // configuration with the resources we have gathered.
        //
        return new TerraformProcessingResult(variables, terraformVariablesJson);
    }

    // This flattens everything into one directory for Terraform to process
    private void copy(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(s -> {
            try {
                if (Files.isRegularFile(s)) {
                    Files.copy(s, dest.resolve(src.relativize(s)), REPLACE_EXISTING);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    // This flattens everything into one directory for Terraform to process
    private void copyPoliciesToWorkDir(Path src, Path workDir) throws IOException {
        Files.walk(src).forEach(s -> {
            try {
                if (s.getFileName().toString().contains("policy")) {
                    Files.copy(s, workDir.resolve(src.relativize(s)), REPLACE_EXISTING);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private List<TerraformVariable> processVariables(String resource, Path terraformVariablesFile, Map<String, Object> configuration) throws Exception {
        List<TerraformVariable> variables = Lists.newArrayList();
        Map<String, Object> entries = new HCLParser().parse(terraformVariablesFile.toFile(), "UTF-8");
        Map<String, Object> variableMap = (Map<String, Object>) entries.get("variable");
        if (variableMap != null) {
            for (Map.Entry<String, Object> variable : variableMap.entrySet()) {
                String name = variable.getKey();
                Map<String, Object> variableInfo = (Map<String, Object>) variable.getValue();
                String type;
                if (variableInfo.get("type") != null) {
                    type = ((Variable) variableInfo.get("type")).name;
                } else {
                    type = "string";
                }
                String description = variableInfo.get("description") != null ? (String) variableInfo.get("description") : "";
                variables.add(new TerraformVariable(name, type, resource, description, configuration.get(name)));
            }
        }
        return variables;
    }

    public static class TerraformVariable {
        public String name;
        public String type;
        public String resource;
        public String description;
        public Object value;

        public TerraformVariable(String name, String type, String resource, String description, Object value) {
            this.name = name;
            this.type = type;
            this.resource = resource;
            this.description = description;
            this.value = value;
        }

        @Override
        public String toString() {
            return "TerraformVariable{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", resource='" + resource + '\'' +
                    ", description='" + description + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
