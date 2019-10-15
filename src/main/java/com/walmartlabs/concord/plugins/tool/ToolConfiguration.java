package com.walmartlabs.concord.plugins.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ToolConfiguration {

  @JsonProperty
  private String command;

  @JsonProperty
  private boolean debug;

  @JsonProperty
  private boolean dryRun;

  public String command() {
    return command;
  }

  public boolean debug() {
    return debug;
  }

  public boolean dryRun() {
    return dryRun;
  }
}
