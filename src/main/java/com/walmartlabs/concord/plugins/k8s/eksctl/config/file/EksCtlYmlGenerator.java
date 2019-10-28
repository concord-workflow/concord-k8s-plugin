package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireeye.k8s.ClusterGenerationRequest;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class EksCtlYmlGenerator {

    private String name;
    private String region;
    private String k8sVersion;
    private ObjectMapper mapper;
    private String terraformOutput;

    public EksCtlYmlGenerator() {}

    public EksCtlYmlGenerator(String name, String region, String k8sVersion) {
        this.name = name;
        this.region = region;
        this.k8sVersion = k8sVersion;
        this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void generate(ClusterGenerationRequest request, String terraformOutput, File outputDirectory) throws IOException {
        Map<String,Object> map = mapper.readValue(terraformOutput, Map.class);
        ClusterInfo clusterInfo = new ClusterInfo(request.getCluster().getName(), request.getCluster().getRegion(), request.getCluster().getK8sVersion(), map);
        generateClusterYml(clusterInfo, new FileOutputStream(new File(outputDirectory, "cluster.yml")));
        generateAutoscalerYml(clusterInfo, new FileOutputStream(new File(outputDirectory, "autoscaler.yml")));
    }

    public void generateClusterYml(ClusterInfo cluster, OutputStream outputStream) throws IOException {
        clusterYml(cluster, outputStream, "eksctl/templates/cluster.mustache");
    }

    public void generateAutoscalerYml(ClusterInfo cluster, OutputStream outputStream) throws IOException {
        clusterYml(cluster, outputStream, "eksctl/templates/autoscaler.mustache");
    }

    private void clusterYml(ClusterInfo cluster, OutputStream outputStream, String template) throws IOException {
        try(Writer writer = new OutputStreamWriter(outputStream)) {
            HashMap<String, Object> scopes = new HashMap<>();
            scopes.put("cluster", cluster);
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(template);
            mustache.execute(writer, scopes);
            writer.flush();
        }
    }

    // These are for testing move these out of here

    public ClusterInfo parse(File file) throws IOException {
        return parse(new FileInputStream(file));
    }

    private ClusterInfo parse(InputStream inputStream) throws IOException {
        return new ClusterInfo(name, region, k8sVersion, mapper.readValue(inputStream, Map.class));
    }
}
