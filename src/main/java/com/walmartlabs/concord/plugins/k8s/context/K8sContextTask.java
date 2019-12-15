package com.walmartlabs.concord.plugins.k8s.context;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Singleton
@Named("k8sContext")
public class K8sContextTask extends TaskSupport {

    private final static Logger logger = LoggerFactory.getLogger(K8sContextTask.class);

    private final List<String> ingressAnnotations;
    private final List<String> postManifests;

    public K8sContextTask() {
        ingressAnnotations = Lists.newArrayList();
        postManifests = Lists.newArrayList();
    }

    public void ingressAnnotation(String ingressAnnotation) {
        logger.info("Adding ingress annotation: {}", ingressAnnotation);
        ingressAnnotations.add(ingressAnnotation);
    }

    public String ingressAnnotations(int indentCount) {
        logger.info("Retrieving {} ingress annotations with indent = {}", ingressAnnotations.size(), indentCount);
        String indent = String.join("", Collections.nCopies(indentCount, " "));
        StringBuffer sb = new StringBuffer(System.lineSeparator());
        int size = ingressAnnotations.size();
        for (int i = 0; i < size; i++) {
            String ingressAnnotation = ingressAnnotations.get(i);
            sb.append(indent).append(ingressAnnotation);
            if (i != (size - 1)) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public List<String> ingressAnnotationsList() {
        return ingressAnnotations;
    }

    public void postManifest(String manifest) {
        postManifests.add(manifest);
    }

    public List<String> postManifests() {
        return postManifests;
    }
}
