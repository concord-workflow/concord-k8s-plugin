package ca.vanzyl.concord.plugins.tool;

import org.immutables.value.Value;

import java.nio.file.Path;

@Value.Immutable
public abstract class ToolInitializationResult {

    public abstract Path executable();

}
