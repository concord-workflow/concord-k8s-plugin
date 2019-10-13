package ca.vanzyl.concord.plugins.k8s;

import com.walmartlabs.concord.sdk.Task;

public interface ToolTask extends Task {

  ToolDescriptor toolDescriptor();
}
