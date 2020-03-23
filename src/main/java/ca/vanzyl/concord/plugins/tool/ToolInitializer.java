package ca.vanzyl.concord.plugins.tool;

import ca.vanzyl.concord.plugins.tool.ToolDescriptor.NamingStyle;
import ca.vanzyl.concord.plugins.tool.ToolDescriptor.Packaging;
import com.walmartlabs.concord.sdk.DependencyManager;
import io.tesla.proviso.archive.UnArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static ca.vanzyl.concord.plugins.tool.ToolDescriptor.Packaging.TARGZ;
import static ca.vanzyl.concord.plugins.tool.ToolDescriptor.Packaging.TARGZ_STRIP;

//
// This has everything to do with initializing a tool for use in
//
// terraform:
// https://releases.hashicorp.com/terraform/%s/terraform_%s_%s_amd64.zip
//
// eksctl
// https://github.com/weaveworks/eksctl/releases/download/latest_release/eksctl_$(uname -s)_amd64.tar.gz"
//
// helm:
// https://get.helm.sh/helm-v2.14.3-linux-amd64.tar.gz
//
// kubectl:
// https://storage.googleapis.com/kubernetes-release/release/v1.16.0/bin/linux/amd64/kubectl
//
// packaging = file, tar.gz, zip
// urlTemplate
// architectureMapper
// where to place it in the workingDirectory
//
@Named
@Singleton
public class ToolInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ToolInitializer.class);

    private final DependencyManager dependencyManager;

    @Inject
    public ToolInitializer(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    //private static final String DEFAULT_TERRAFORM_VERSION = "0.12.5";
    //private static final String DEFAULT_TOOL_URL_TEMPLATE = "https://releases.hashicorp.com/terraform/%s/terraform_%s_%s_amd64.zip";

    // During the init we will download the version of Terraform specified by the user if defined, otherwise
    // we will download the default version. Terraform URLs look like the following:
    //
    // https://releases.hashicorp.com/terraform/0.12.5/terraform_0.12.5_linux_amd64.zip
    // https://releases.hashicorp.com/terraform/0.11.2/terraform_0.11.2_linux_amd64.zip
    //
    // So we can generalize to:
    //
    // https://releases.hashicorp.com/terraform/${version}/terraform_${version}_linux_amd64.zip
    //
    // We will also allow the user to specify the full URL if they want to download the tool zip from
    // and internal repository manager or other internally managed host.
    //

    public static String os(ToolDescriptor toolDescriptor) {

        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("mac") >= 0) {
            os = "darwin";
        } else if (os.indexOf("nux") >= 0) {
            os = "linux";
        } else if (os.indexOf("win") >= 0) {
            os = "windows";
        } else if (os.indexOf("sunos") >= 0) {
            os = "solaris";
        } else {
            throw new IllegalArgumentException("Your operating system is not supported: " + os);
        }

        // The os name is capitalized as in darwin needs to become Darwin for the URL as is the case with EKSCTL
        // where the URL is something like:
        //
        // https://github.com/weaveworks/eksctl/releases/download/0.7.0/eksctl_Darwin_amd64.tar.gz
        //
        // NOTE: On Linux/OS getting $(uname) is already capitalized which is where this convention likely
        // comes from.
        //
        if (toolDescriptor.namingStyle().equals(NamingStyle.CAPITALIZE)) {
            return os.substring(0, 1).toUpperCase() + os.substring(1);
        }

        return os;
    }

    public ToolInitializationResult initialize(Path workDir, ToolDescriptor toolDescriptor) throws Exception {
        return initialize(workDir, toolDescriptor, false);
    }

    public ToolInitializationResult initialize(Path workDir, ToolDescriptor toolDescriptor, boolean debug) throws Exception {

        Path targetDirectory;
        if (toolDescriptor.location() != null) {
            targetDirectory = Paths.get(toolDescriptor.location());
        } else {
            targetDirectory = workDir.resolve("." + toolDescriptor.id()); // .eksctl, .terraform, .helm, etc
        }

        if (!Files.exists(targetDirectory)) {
            Files.createDirectories(targetDirectory);
        }

        Path executable = targetDirectory.resolve(toolDescriptor.executable());
        if (Files.exists(executable)) {
            if (debug) {
                logger.info("init -> using the existing binary {}", workDir.relativize(executable));
            }
            return ImmutableToolInitializationResult.builder().executable(executable).build();
        }

        String toolUrl = resolveToolUrl(toolDescriptor);
        logger.info("Retrieving {} package from {} ...", toolDescriptor.name(), toolUrl);
        Path executablePackage = dependencyManager.resolve(new URI(toolUrl));
        logger.info("Retrieved {} package and saved to {} ...", toolDescriptor.name(), executablePackage);

        if (debug) {
            logger.info("init -> extracting the executable into {}", workDir.relativize(targetDirectory));
        }

        if (executablePackage == null) {
            throw new IllegalStateException(String.format("The Terraform archive '%s' does not appear to be valid.", executablePackage));
        }

        if (toolDescriptor.packaging().equals(TARGZ) || toolDescriptor.packaging().equals(TARGZ_STRIP) || toolDescriptor.packaging().equals(Packaging.ZIP)) {
            boolean useRoot = toolDescriptor.packaging().equals(TARGZ_STRIP) ? false : true;
            logger.info("Unarchiving {} to {} ...", executablePackage.getFileName(), targetDirectory);
            UnArchiver unArchiver = UnArchiver.builder()
                    .useRoot(useRoot)
                    .build();
            unArchiver.unarchive(executablePackage.toFile(), targetDirectory.toFile());
        } else {
            // Copy the single file over and make executable
            Files.copy(executablePackage, executable, StandardCopyOption.REPLACE_EXISTING);
            executable.toFile().setExecutable(true);
        }

        return ImmutableToolInitializationResult.builder().executable(executable).build();
    }

    // During the init we will download the version of Terraform specified by the user if defined, otherwise
    // we will download the default version. Terraform URLs look like the following:
    //
    // https://releases.hashicorp.com/terraform/0.12.5/terraform_0.12.5_linux_amd64.zip
    // https://releases.hashicorp.com/terraform/0.11.2/terraform_0.11.2_linux_amd64.zip
    //
    // So we can generalize to:
    //
    // https://releases.hashicorp.com/terraform/%s/terraform_%s_linux_amd64.zip
    //
    // We will also allow the user to specify the full URL if they want to download the tool zip from
    // and internal repository manager or other internally managed host.
    //
    private String resolveToolUrl(ToolDescriptor toolDescriptor) {
        String toolUrl = toolDescriptor.userSpecifiedUrl();
        if (toolUrl != null && !toolUrl.isEmpty()) {
            //
            // The user has explicitly specified a URL from where to download the tool.
            //
            return toolUrl;
        }
        //
        // Check to see if the user has specified a version of the tool to use, if not use the default version.
        //
        String toolVersion = toolDescriptor.version() != null ? toolDescriptor.version() : toolDescriptor.defaultVersion();

        return toolDescriptor.urlTemplate()
                .replaceAll("\\{version\\}", toolVersion)
                .replaceAll("\\{os\\}", os(toolDescriptor))
                .replaceAll("\\{arch\\}", toolDescriptor.architecture());
    }
}
