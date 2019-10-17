package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireeye.k8s.ClusterGenerationRequest;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class EksCtlClusterYmlGenerator {

    private final static String EKS_CONFIG_TEMPLATE = "eksctl/templates/cluster.mustache";
    private final static String AUTOSCALER_TEMPLATE = "eksctl/templates/cluster.mustache";

    private String name;
    private String region;
    private String k8sVersion;
    private ObjectMapper mapper;

    public EksCtlClusterYmlGenerator(ClusterGenerationRequest request) {
        this(request.getCluster().getName(), request.getCluster().getRegion(), request.getCluster().getK8sVersion());
    }

    public EksCtlClusterYmlGenerator(String name, String region, String k8sVersion) {
        this.name = name;
        this.region = region;
        this.k8sVersion = k8sVersion;
        this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ClusterInfo parse(File file) throws IOException {
        return parse(new FileInputStream(file));
    }

    public ClusterInfo parse(InputStream inputStream) throws IOException {
        return new ClusterInfo(name, region, k8sVersion, mapper.readValue(inputStream, Map.class));
    }

    public void clusterYml(ClusterInfo cluster, OutputStream outputStream) throws IOException {
        mustache(cluster, outputStream, EKS_CONFIG_TEMPLATE);
    }

    public void autoscalerYml(ClusterInfo cluster, OutputStream outputStream) throws IOException {
        mustache(cluster, outputStream, AUTOSCALER_TEMPLATE);
    }

    public void mustache(ClusterInfo cluster, OutputStream outputStream, String template) throws IOException {
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
