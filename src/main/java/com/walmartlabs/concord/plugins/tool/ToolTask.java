package com.walmartlabs.concord.plugins.tool;

import com.walmartlabs.concord.sdk.Task;

public interface ToolTask extends Task {

  ToolDescriptor toolDescriptor();
}
