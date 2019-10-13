package ca.vanzyl.concord.plugins.k8s;

import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
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
  public abstract String userSpecifiedUrl();

  public abstract NamingStyle namingStyle();

  enum Packaging {
    FILE,
    TARGZ,
    TARGZ_STRIP,
    ZIP
  }

  enum NamingStyle {
    LOWER,
    UPPER,
    CAPITALIZE
  }
}