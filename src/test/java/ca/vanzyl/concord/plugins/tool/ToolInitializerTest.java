package ca.vanzyl.concord.plugins.tool;

import com.walmartlabs.concord.plugins.ConcordTestSupport;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class ToolInitializerTest extends ConcordTestSupport
{

    @Test
    public void validateToolInitializerWithHelm() throws Exception {

        Path workingDirectory = Files.createTempDirectory("concord");
        deleteDirectory(workingDirectory);

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("helm"));
        ToolDescriptor toolDescriptor = ToolTaskSupport.fromResource("helm");
        ToolInitializationResult result = toolInitializer.initialize(workingDirectory, toolDescriptor);

        assertTrue(result.executable().toFile().exists());
        assertTrue(Files.isExecutable(result.executable()));

        deleteDirectory(workingDirectory);
    }

    @Test
    public void validateToolInitializerWithKubeCtl() throws Exception {

        Path workingDirectory = Files.createTempDirectory("concord");
        deleteDirectory(workingDirectory);

        ToolInitializer toolInitializer = new ToolInitializer(new OKHttpDownloadManager("kubectl"));
        ToolDescriptor toolDescriptor = ToolTaskSupport.fromResource("kubectl");
        ToolInitializationResult result = toolInitializer.initialize(workingDirectory, toolDescriptor);

        assertTrue(result.executable().toFile().exists());
        assertTrue(Files.isExecutable(result.executable()));

        deleteDirectory(workingDirectory);
    }
}
