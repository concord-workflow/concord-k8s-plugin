package com.walmartlabs.concord.plugins.k8s.context;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.walmartlabs.concord.plugins.tool.ImmutableToolDescriptor;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Value.Immutable
@JsonDeserialize(as = ImmutableK8sCluster.class)
public abstract class K8sCluster {

    public abstract String id();

    public abstract Set<String> ingressAnnotations();

    public abstract Set<String> postManifests();
}
