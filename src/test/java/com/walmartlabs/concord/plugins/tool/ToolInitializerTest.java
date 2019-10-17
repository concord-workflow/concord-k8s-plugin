package com.walmartlabs.concord.plugins.tool;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.walmartlabs.TestSupport;
import com.walmartlabs.concord.common.IOUtils;
import com.walmartlabs.concord.plugins.k8s.eksctl.EksCtlTask;
import com.walmartlabs.concord.plugins.k8s.eksctl.commands.Create;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.DependencyManager;
import com.walmartlabs.concord.sdk.ImmutableBucketInfo;
import com.walmartlabs.concord.sdk.LockService;
import com.walmartlabs.concord.sdk.MockContext;
import com.walmartlabs.concord.sdk.ObjectStorage;
import com.walmartlabs.concord.sdk.SecretService;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.walmartlabs.concord.plugins.k8s.eksctl.EksCtlTest.mapBuilder;
import static com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY;
import static com.walmartlabs.concord.sdk.Constants.Request.PROCESS_INFO_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToolInitializerTest extends TestSupport {

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
