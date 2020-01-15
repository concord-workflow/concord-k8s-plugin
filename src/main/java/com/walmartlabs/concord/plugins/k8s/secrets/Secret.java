package com.walmartlabs.concord.plugins.k8s.secrets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableSecret.class)
public abstract class Secret {

    public abstract String name();

    public abstract String value();

    public abstract String description();
}