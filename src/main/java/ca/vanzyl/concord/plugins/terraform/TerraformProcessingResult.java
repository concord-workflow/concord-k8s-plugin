package ca.vanzyl.concord.plugins.terraform;

import java.util.List;

public class TerraformProcessingResult {

    private final List<TerraformProcessor.TerraformVariable> variables;
    private final String terraformVariablesJson;

    public TerraformProcessingResult(List<TerraformProcessor.TerraformVariable> variables, String terraformVariableJson) {
        this.variables = variables;
        this.terraformVariablesJson = terraformVariableJson;
    }

    public List<TerraformProcessor.TerraformVariable> variables() {
        return variables;
    }

    public String terraformVariablesJson() {
        return terraformVariablesJson;
    }
}
