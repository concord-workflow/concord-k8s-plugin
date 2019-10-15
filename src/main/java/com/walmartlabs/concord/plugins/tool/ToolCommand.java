package com.walmartlabs.concord.plugins.tool;

import java.util.List;

public interface ToolCommand {

  List<String> generateCommandLineArguments(String command) throws Exception;

}
