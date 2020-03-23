package ca.vanzyl.concord.plugins.k8s.context;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonDeserialize(as = ImmutableK8sCluster.class)
public abstract class K8sCluster {

    public abstract String id();

    public abstract Set<String> enabledFeatures();

    public abstract Set<String> ingressAnnotations();

    public abstract Set<String> postManifests();
}
