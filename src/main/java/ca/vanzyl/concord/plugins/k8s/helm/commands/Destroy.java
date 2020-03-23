package com.walmartlabs.concord.plugins.k8s.helm.commands;

import com.walmartlabs.concord.plugins.tool.ToolCommandSupport;
import com.walmartlabs.concord.plugins.tool.Value;

import javax.inject.Named;

@Named("helm/destroy")
@Value("list --short | xargs -L1 helm delete")
public class Destroy extends ToolCommandSupport {
}
