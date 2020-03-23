package ca.vanzyl.concord.plugins.directory;

import org.apache.commons.compress.utils.Lists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DirectoryUtils {

    public static List<String> scanDirectory(String directoryName) throws IOException {
        List<String> files = Lists.newArrayList();
        Path directory = Paths.get(directoryName);
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .filter(path -> Files.isRegularFile(path))
                    .forEach(path -> files.add(path.toString()));
        }
        return files;
    }
}
