package ca.vanzyl.concord.plugins.k8s.helm3.config;

import ca.vanzyl.concord.plugins.tool.annotations.OptionWithEquals;
import ca.vanzyl.concord.plugins.tool.annotations.Value;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Add {

    @JsonProperty
    @OptionWithEquals(name = "--username")
    private String username;

    @JsonProperty
    @OptionWithEquals(name = "--password")
    private String password;

    @JsonProperty
    @Value
    private String name;

    @JsonProperty
    @Value
    private String url;

    public String name() { return name; }

    public String url() { return url; }
}
