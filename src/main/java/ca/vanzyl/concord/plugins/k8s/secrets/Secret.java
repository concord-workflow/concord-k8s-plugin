package ca.vanzyl.concord.plugins.k8s.secrets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonDeserialize(as = ImmutableSecret.class)
public abstract class Secret {

    public abstract String name();

    public abstract String value();

    @Nullable
    public abstract String description();
}