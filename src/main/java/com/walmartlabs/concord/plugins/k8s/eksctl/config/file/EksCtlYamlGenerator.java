package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import static ca.vanzyl.concord.plugins.Utils.createDirectoryIfNotPresent;

public class EksCtlYamlGenerator {

    public void generate(EksCtlYamlData cluster, File eksctlYamlFile) throws IOException {
        createDirectoryIfNotPresent(eksctlYamlFile.getParentFile().toPath());
        generateClusterYml(cluster, new FileOutputStream(eksctlYamlFile));
    }

    private void generateClusterYml(EksCtlYamlData cluster, OutputStream outputStream) throws IOException {
        clusterYml(cluster, outputStream, "eksctl/templates/cluster.mustache");
    }

    private void clusterYml(EksCtlYamlData cluster, OutputStream outputStream, String template) throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream)) {
            HashMap<String, Object> scopes = new HashMap<>();
            scopes.put("cluster", cluster);
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(template);
            mustache.execute(writer, scopes);
            writer.flush();
        }
    }
}
