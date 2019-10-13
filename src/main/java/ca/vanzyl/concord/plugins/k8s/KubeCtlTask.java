package ca.vanzyl.concord.plugins.k8s;


import ca.vanzyl.concord.plugins.k8s.ToolDescriptor.NamingStyle;
import ca.vanzyl.concord.plugins.k8s.ToolDescriptor.Packaging;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.InjectVariable;
import com.walmartlabs.concord.sdk.LockService;
import com.walmartlabs.concord.sdk.MapUtils;
import com.walmartlabs.concord.sdk.ObjectStorage;
import com.walmartlabs.concord.sdk.SecretService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("helm")
public class KubeCtlTask implements ToolTask {

  public static final String DEFAULT_VERSION = "v1.16.0";
  public static final String URL_TEMPLATE = "https://storage.googleapis.com/kubernetes-release/release/{version}/bin/{os}/{arch}/kubectl";

  private static final Logger logger = LoggerFactory.getLogger(KubeCtlTask.class);

  private final LockService lockService;
  private final ObjectStorage objectStorage;
  private final SecretService secretService;
  private final ObjectMapper objectMapper;
  private final ToolInitializer toolInitializer;

  @InjectVariable("helmParams")
  private Map<String, Object> defaults;

  @Inject
  public KubeCtlTask(LockService lockService, ObjectStorage objectStorage, SecretService secretService, ToolInitializer toolInitializer) {
    this.lockService = lockService;
    this.objectStorage = objectStorage;
    this.secretService = secretService;
    this.objectMapper = new ObjectMapper();
    this.toolInitializer = toolInitializer;
  }

  @Override
  public ToolDescriptor toolDescriptor() {
    return ImmutableToolDescriptor.builder()
        .id("kubectl")
        .name("KubeCtl")
        .executable("kubectl")
        .architecture("amd64")
        .namingStyle(NamingStyle.LOWER)
        .packaging(Packaging.FILE)
        .defaultVersion(DEFAULT_VERSION)
        .urlTemplate(URL_TEMPLATE)
        .build();
  }

  public void execute(Context ctx) throws Exception {

    String instanceId = (String) ctx.getVariable(com.walmartlabs.concord.sdk.Constants.Context.TX_ID_KEY);

    Map<String, Object> cfg = createCfg(ctx);

    Path workDir = Paths.get((String) ctx.getVariable(com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY));
    if (workDir == null) {
      throw new IllegalArgumentException("Can't determine the current '" + com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY + "'");
    }

    boolean debug = MapUtils.get(cfg, Constants.DEBUG_KEY, false, Boolean.class);

    ToolInitializationResult result = toolInitializer.initialize(workDir, toolDescriptor(), debug);

    Command command = new Command(workDir, result.executable(), debug, new HashMap<>());
    if (debug) {
      command.exec("version", "version");
    }
  }

  private Map<String, Object> createCfg(Context ctx) {
    Map<String, Object> m = new HashMap<>(defaults != null ? defaults : Collections.emptyMap());
    put(m, com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY, ctx);
    for (String k : Constants.ALL_IN_PARAMS) {
      put(m, k, ctx);
    }
    return m;
  }

  private static void put(Map<String, Object> m, String k, Context ctx) {
    Object v = ctx.getVariable(k);
    if (v == null) {
      return;
    }
    m.put(k, v);
  }
}
