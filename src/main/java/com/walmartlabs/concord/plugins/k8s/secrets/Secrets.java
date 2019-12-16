package com.walmartlabs.concord.plugins.k8s.secrets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize(as = ImmutableSecrets.class)
public abstract class Secrets {

    @JsonProperty("secrets")
    public abstract List<Secret> list();
}