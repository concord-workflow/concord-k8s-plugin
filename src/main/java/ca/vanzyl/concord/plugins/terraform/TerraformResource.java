package ca.vanzyl.concord.plugins.terraform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TerraformResource {

    private String type;
    private Map<String,Object> configuration;

    @JsonCreator
    public TerraformResource(@JsonProperty("type") String type, @JsonProperty("variables") Map<String, Object> configuration) {
        this.type = type;
        this.configuration = configuration;
    }

    public String type() {
        return type;
    }

    public Map<String, Object> configuration() {
        return configuration;
    }
}
