package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static ca.vanzyl.concord.plugins.Utils.createDirectoryIfNotPresent;

public class EksCtlYamlGenerator {

    public void generate(Map<String, Object> clusterRequest, EksCtlYamlData cluster, File eksctlYamlFile) throws IOException {
        createDirectoryIfNotPresent(eksctlYamlFile.getParentFile().toPath());
        generateClusterYml(clusterRequest, cluster, new FileOutputStream(eksctlYamlFile));
    }

    private void generateClusterYml(Map<String, Object> clusterRequest, EksCtlYamlData cluster, OutputStream outputStream) throws IOException {
        clusterYml(clusterRequest, cluster, outputStream, "eksctl/templates/cluster.mustache");
    }

    private void clusterYml(Map<String, Object> clusterRequest, EksCtlYamlData cluster, OutputStream outputStream, String template) throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream)) {
            HashMap<String, Object> scopes = new HashMap<>();
            scopes.put("cluster", cluster);
            scopes.put("clusterRequest", clusterRequest);
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(template);
            mustache.execute(writer, scopes);
            writer.flush();
        }
    }
}
