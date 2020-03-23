package ca.vanzyl.concord.plugins.tool;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonDeserialize(as = ImmutableToolDescriptor.class)
public abstract class ToolDescriptor {

    public abstract String id();

    public abstract String name();

    public abstract String executable();

    public abstract String architecture();

    public abstract Packaging packaging();

    @Nullable
    public abstract String version();

    public abstract String urlTemplate();

    public abstract String defaultVersion();

    @Nullable
    public abstract String location();

    @Nullable
    public abstract String userSpecifiedUrl();

    public abstract NamingStyle namingStyle();

    public enum Packaging {
        FILE,
        TARGZ,
        TARGZ_STRIP,
        ZIP
    }

    public enum NamingStyle {
        LOWER,
        UPPER,
        CAPITALIZE
    }
}