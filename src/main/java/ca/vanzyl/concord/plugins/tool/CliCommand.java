package ca.vanzyl.concord.plugins.tool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.airlift.units.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CliCommand
{
    private Path workDir;
    private List<String> args;
    private Map<String, String> envars;
    private final Duration timeout;

    public CliCommand(String args)
            throws Exception
    {
        // This could be a little more robust
        this(Arrays.asList(args.split(" ")), ImmutableSet.of(0), Paths.get(new File("").getAbsolutePath()),
                ImmutableMap.of(),
                Duration.succinctDuration(30, TimeUnit.SECONDS));
    }

    public CliCommand(String args, Set<Integer> successfulExitCodes, Path workDir, Map<String, String> envars, Duration timeout)
            throws Exception
    {
        this(Arrays.asList(args.split(" ")), successfulExitCodes, workDir, envars, timeout);
    }

    public CliCommand(List<String> args, Set<Integer> successfulExitCodes, Path workDir, Map<String, String> envars, Duration timeout)
            throws Exception
    {
        this.workDir = workDir;
        this.args = args;
        this.envars = envars;
        this.timeout = timeout;
    }

    public Result execute()
            throws Exception
    {
        return execute(Executors.newCachedThreadPool());
    }

    public Result execute(ExecutorService executor)
            throws Exception
    {
        ProcessBuilder pb = new ProcessBuilder(args).directory(workDir.toFile());
        Map<String, String> combinedEnv = new HashMap<>(envars);
        pb.environment().putAll(combinedEnv);
        Process p = pb.start();

        Future<String> stderr = executor.submit(new StreamReader(p.getErrorStream()));
        Future<String> stdout = executor.submit(new StreamReader(p.getInputStream()));

        int code = p.waitFor();
        return new Result(code, stdout.get(), stderr.get());
    }

    public List<String> getCommand()
    {
        return args;
    }

    private static class StreamReader
            implements Callable<String>
    {
        private final InputStream in;

        private StreamReader(InputStream in)
        {
            this.in = in;
        }

        @Override
        public String call()
                throws Exception
        {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
            }
            return sb.toString();
        }
    }

    public static class Result
    {
        private final int code;
        private final String stdout;
        private final String stderr;

        public Result(int code, String stdout, String stderr)
        {
            this.code = code;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getCode()
        {
            return code;
        }

        public String getStdout()
        {
            return stdout;
        }

        public String getStderr()
        {
            return stderr;
        }
    }
}
