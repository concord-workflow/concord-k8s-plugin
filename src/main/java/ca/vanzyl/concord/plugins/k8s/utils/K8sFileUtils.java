package ca.vanzyl.concord.plugins.k8s.utils;

import ca.vanzyl.concord.plugins.TaskSupport;

import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Comparator.reverseOrder;

@Named("k8sFileUtils")
public class K8sFileUtils extends TaskSupport {

    public void deleteDirectory(String directory) throws Exception {
        Path directoryToDelete = Paths.get(directory);
        if(Files.exists(directoryToDelete)) {
            Files.walk(directoryToDelete)
                    .sorted(reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
