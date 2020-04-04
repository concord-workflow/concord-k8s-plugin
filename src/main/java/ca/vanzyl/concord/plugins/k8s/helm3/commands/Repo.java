package ca.vanzyl.concord.plugins.k8s.helm3.commands;

import ca.vanzyl.concord.plugins.k8s.helm3.config.Add;
import ca.vanzyl.concord.plugins.k8s.helm3.config.Update;
import ca.vanzyl.concord.plugins.tool.ToolCommandSupport;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Named;

@Named("helm3/repo")
public class Repo extends ToolCommandSupport {

    @JsonProperty
    private Add add;

    public Add add() { return add; }

    @JsonProperty
    private Update update;

    public Update update() {return update;}

}
