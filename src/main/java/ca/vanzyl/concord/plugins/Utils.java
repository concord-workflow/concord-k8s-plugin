package ca.vanzyl.concord.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class Utils {

    public static void createDirectoryIfNotPresent(Path directory) throws IOException {
        System.out.println(directory);
        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
        }
    }

    public static void emptyDirectoryIfContentPresent(Path directory) throws IOException {
        if (Files.exists(directory)) {
            delete(directory);
        }
        Files.createDirectory(directory);
    }

    public static void delete(Path directory) throws IOException {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
