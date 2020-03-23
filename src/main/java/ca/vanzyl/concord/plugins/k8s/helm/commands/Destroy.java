package ca.vanzyl.concord.plugins.k8s.helm.commands;

import ca.vanzyl.concord.plugins.tool.ToolCommandSupport;
import ca.vanzyl.concord.plugins.tool.annotations.Value;

import javax.inject.Named;

@Named("helm/destroy")
@Value("list --short | xargs -L1 helm delete")
public class Destroy extends ToolCommandSupport {
}
