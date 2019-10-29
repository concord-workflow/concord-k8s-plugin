package com.walmartlabs.concord.plugins.k8s.eksctl.config.file;

import com.fireeye.k8s.ClusterGenerationRequest;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class EksCtlYmlGenerator {

    public void generate(ClusterGenerationRequest request, Map<String,Object> map, File outputDirectory) throws IOException {
        ClusterInfo clusterInfo = new ClusterInfo(request.getCluster().getName(), request.getCluster().getRegion(), request.getCluster().getK8sVersion(), map);
        generateClusterYml(clusterInfo, new FileOutputStream(new File(outputDirectory, "cluster.yml")));
        generateAutoscalerYml(clusterInfo, new FileOutputStream(new File(outputDirectory, "autoscaler.yml")));
    }

    public void generate(ClusterGenerationRequest request, String terraformOutput, File outputDirectory) throws IOException {
        Map<String, Object> map = SimpleObjectMapper.map(terraformOutput);
        ClusterInfo clusterInfo = new ClusterInfo(request.getCluster().getName(), request.getCluster().getRegion(), request.getCluster().getK8sVersion(), map);
        generateClusterYml(clusterInfo, new FileOutputStream(new File(outputDirectory, "cluster.yml")));
        generateAutoscalerYml(clusterInfo, new FileOutputStream(new File(outputDirectory, "autoscaler.yml")));
    }

    public void generate(ClusterInfo cluster, File outputDirectory) throws IOException {
        generateClusterYml(cluster, new FileOutputStream(new File(outputDirectory, "cluster.yml")));
        generateAutoscalerYml(cluster, new FileOutputStream(new File(outputDirectory, "autoscaler.yml")));
    }

    private void generateClusterYml(ClusterInfo cluster, OutputStream outputStream) throws IOException {
        clusterYml(cluster, outputStream, "eksctl/templates/cluster.mustache");
    }

    private void generateAutoscalerYml(ClusterInfo cluster, OutputStream outputStream) throws IOException {
        clusterYml(cluster, outputStream, "eksctl/templates/autoscaler.mustache");
    }

    private void clusterYml(ClusterInfo cluster, OutputStream outputStream, String template) throws IOException {
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
