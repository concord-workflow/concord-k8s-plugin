package ca.vanzyl.concord.plugins.k8s;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Command {

  private static final Logger log = LoggerFactory.getLogger(Command.class);

  private final boolean debug;
  private final Path executable;
  private final Path workingDirectory;
  private final Map<String, String> baseEnv;
  private final ExecutorService executor;

  /**
   * @param workingDirectory the process' working directory. Used to store temporary files
   * @param debug enable/disable additional debug output
   */
  public Command(Path workingDirectory, Path executable, boolean debug, Map<String, String> baseEnv) throws Exception {
    this.workingDirectory = workingDirectory;
    this.executable = executable;
    this.debug = debug;
    this.baseEnv = baseEnv;
    this.executor = Executors.newCachedThreadPool();
  }

  public Result exec(String logPrefix, String... args) throws Exception {
    return exec(logPrefix, false, Collections.emptyMap(), Arrays.asList(args));
  }

  public Result exec(String logPrefix, boolean silent, Map<String, String> env, List<String> args) throws Exception {
    List<String> cmd = new ArrayList<>();
    cmd.add(executable.toAbsolutePath().toString());
    cmd.addAll(args);

    if (debug) {
      log.info("exec -> {} in {}", String.join(" ", cmd), workingDirectory);
    }

    ProcessBuilder pb = new ProcessBuilder(cmd)
        .directory(workingDirectory.toFile());

    Map<String, String> combinedEnv = new HashMap<>(baseEnv);
    combinedEnv.putAll(env);

    if (debug) {
      log.info("exec -> using env: {}", combinedEnv);
    }

    pb.environment().putAll(combinedEnv);

    Process p = pb.start();

    Future<String> stderr = executor.submit(new StreamReader(logPrefix, false, p.getErrorStream()));
    Future<String> stdout = executor.submit(new StreamReader(logPrefix, silent, p.getInputStream()));

    int code = p.waitFor();
    return new Result(code, stdout.get(), stderr.get());
  }

  private static void log(String prefix, String s) {
    System.out.print("\u001b[34mterraform\u001b[0m " + prefix + ": ");
    System.out.print(s);
    System.out.println();
  }

  private static String removeAnsiColors(String s) {
    return s.replaceAll("\u001B\\[[;\\d]*m", "");
  }

  private static class StreamReader implements Callable<String> {

    private final String logPrefix;
    private final boolean silent;
    private final InputStream in;

    private StreamReader(String logPrefix, boolean silent, InputStream in) {
      this.logPrefix = logPrefix;
      this.silent = silent;
      this.in = in;
    }

    @Override
    public String call() throws Exception {
      StringBuilder sb = new StringBuilder();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

        String line;
        while ((line = reader.readLine()) != null) {
          if (!silent) {
            log(logPrefix, line);
          }

          sb.append(removeAnsiColors(line))
              .append(System.lineSeparator());
        }
      }

      return sb.toString();
    }
  }

  public static class Result {

    private final int code;
    private final String stdout;
    private final String stderr;

    public Result(int code, String stdout, String stderr) {
      this.code = code;
      this.stdout = stdout;
      this.stderr = stderr;
    }

    public int getCode() {
      return code;
    }

    public String getStdout() {
      return stdout;
    }

    public String getStderr() {
      return stderr;
    }
  }
}
