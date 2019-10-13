package ca.vanzyl.concord.plugins.k8s;

public final class Constants {

  public static final String ACTION_KEY = "action";
  public static final String BACKEND_KEY = "backend";
  public static final String DEBUG_KEY = "debug";
  public static final String DEFAULT_ENV_KEY = "defaultEnv";
  public static final String DESTROY_KEY = "destroy";
  public static final String DIR_KEY = "dir";
  public static final String EXTRA_ENV_KEY = "extraEnv";
  public static final String EXTRA_VARS_KEY = "extraVars";
  public static final String VARS_FILES = "varFiles";
  public static final String GIT_SSH_KEY = "gitSsh";
  public static final String IGNORE_ERRORS_KEY = "ignoreErrors";
  public static final String MODULE_KEY = "module";
  public static final String PLAN_KEY = "plan";
  public static final String RESULT_KEY = "result";
  public static final String SAVE_OUTPUT_KEY = "saveOutput";
  public static final String STATE_ID_KEY = "stateId";
  public static final String VERBOSE_KEY = "verbose";
  public static final String TOOL_VERSION_KEY = "toolVersion";
  public static final String TOOL_URL_KEY = "toolUrl";

  public static final String[] ALL_IN_PARAMS = {ACTION_KEY, BACKEND_KEY, DEBUG_KEY, DEFAULT_ENV_KEY, DESTROY_KEY,
      DIR_KEY, EXTRA_ENV_KEY, EXTRA_VARS_KEY, VARS_FILES, GIT_SSH_KEY, IGNORE_ERRORS_KEY, MODULE_KEY, PLAN_KEY,
      RESULT_KEY, SAVE_OUTPUT_KEY, STATE_ID_KEY, VERBOSE_KEY, TOOL_VERSION_KEY, TOOL_URL_KEY};

  private Constants() {
  }
}
