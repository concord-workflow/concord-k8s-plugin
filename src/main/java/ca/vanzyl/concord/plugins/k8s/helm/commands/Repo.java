package ca.vanzyl.concord.plugins.k8s.helm.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import ca.vanzyl.concord.plugins.k8s.helm.config.Add;
import ca.vanzyl.concord.plugins.k8s.helm.config.Update;
import ca.vanzyl.concord.plugins.tool.ToolCommandSupport;

import javax.inject.Named;

@Named("helm/repo")
public class Repo extends ToolCommandSupport {

    @JsonProperty
    private Add add;

    public Add add() { return add; }

    @JsonProperty
    private Update update;

    public Update update() {return update;}

}
