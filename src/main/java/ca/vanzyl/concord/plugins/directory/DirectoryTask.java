package ca.vanzyl.concord.plugins.directory;

import ca.vanzyl.concord.plugins.TaskSupport;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static ca.vanzyl.concord.plugins.directory.DirectoryUtils.scanDirectory;

@Named("directoryTool")
public class DirectoryTask extends TaskSupport {

    public boolean exists(String directoryName) {
        return Files.exists(Paths.get(directoryName));
    }

    public List<String> scan(String directoryName) throws IOException {
        return scanDirectory(directoryName);
    }
}
