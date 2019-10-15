package com.walmartlabs.concord.plugins.tool;

import java.nio.file.Path;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ToolInitializationResult {

  public abstract Path executable();

}
