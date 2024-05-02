package io.quarkus.ts.startstop.utils;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.quarkus.ts.startstop.StartStopTest.BASE_DIR;
import static io.quarkus.ts.startstop.utils.Commands.isThisLinux;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;

public final class AsyncProfiler {

    private static final Logger LOGGER = Logger.getLogger(AsyncProfiler.class.getName());
    private final File lib;
    private final File exe;
    private final String agentConfig;

    private AsyncProfiler(File lib, File exe, String agentConfig) {
        this.lib = lib;
        this.exe = exe;
        this.agentConfig = agentConfig;
    }

    public List<String> createJavaProfiledRunCommand(String[] baseCommand) {
        boolean javaCmd = false;
        List<String> runCmd = new ArrayList<>(baseCommand.length + 1);
        for (String cmdPart : baseCommand) {
            runCmd.add(cmdPart);
            if (cmdPart.equals(Commands.JAVA_BIN)) {
                javaCmd = true;
                runCmd.add("-agentpath:" + lib.getAbsolutePath() + "=" + agentConfig);
            }
        }
        if (!javaCmd) {
            throw new IllegalArgumentException("No java command found in the base command");
        }
        return Collections.unmodifiableList(runCmd);
    }

    public void stopProfing(File appDir, MvnCmds mvnCmds, Process app, int id) {
        try {
            File profilingOutputDir = getProfilingOutputDir(appDir);
            if (!Files.exists(profilingOutputDir.toPath())) {
                Files.createDirectory(profilingOutputDir.toPath());
            }
            String profilerOutputFullPath = profilingOutputDir.getAbsolutePath() + File.separator + mvnCmds.name().toLowerCase() + "-run-" + id + ".html";
            LOGGER.infof("Attaching profiler agent to the JVM process. Output HTML: %s", profilerOutputFullPath);
            LOGGER.info("Stopping the profiler agent...");
            Process stopProfiling = runCommand(List.of(exe.getAbsolutePath(), "stop", "-f", profilerOutputFullPath, "" + app.pid()), appDir, null);
            long initiateStopAt = stopProfiling.info().startInstant().get().toEpochMilli();
            if (stopProfiling.isAlive() && !stopProfiling.waitFor(10, TimeUnit.SECONDS)) {
                LOGGER.warn("Profiler agent did not stop in 10 seconds");
            } else {
                final long stoppingDuration = System.currentTimeMillis() - initiateStopAt;
                final long cpuTimeMs = app.info().totalCpuDuration().get().toMillis();
                LOGGER.infof("CPU time of the profiled process: %d ms", cpuTimeMs);
                LOGGER.infof("Stopping the profiler agent tooks %d ms", stoppingDuration);
                if (stopProfiling.exitValue() != 0) {
                    LOGGER.warn("Profiler agent did not stop successfully: exitValue = " + stopProfiling.exitValue());
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to stop the profiler agent", t);
            throw new RuntimeException(t);
        }
    }

    public void archiveProfilingResults(String cn, String mn, File appDir) {
        File profilingOutputDir = getProfilingOutputDir(appDir);
        if (!Files.exists(profilingOutputDir.toPath())) {
            return;
        }
        try {
            archiveLog(cn, mn, profilingOutputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void cleanProfilingResults(Apps app) {
        String profiling = BASE_DIR + File.separator + app.dir + File.separator + "profiling";
        Commands.cleanDirOrFile(profiling);
    }

    private static File getProfilingOutputDir(File appDir) {
        File profilingOutputDir = new File(appDir.getAbsolutePath() + File.separator + "profiling");
        return profilingOutputDir;
    }

    public static Optional<AsyncProfiler> create() {
        if (!isThisLinux) {
            return Optional.empty();
        }
        String apDirFullPath = getAsyncProfilerDir();
        if (apDirFullPath == null) {
            return Optional.empty();
        }
        File apDir = new File(apDirFullPath);
        if (!apDir.isDirectory()) {
            throw new IllegalArgumentException(apDirFullPath + " is not a directory");
        }
        // check if the bin/asProf and lib/libasyncProfiler.so are present
        File asProf = new File(apDir, "bin" + File.separator + "asprof");
        if (!asProf.exists() || !asProf.canExecute() || !asProf.isFile()) {
            throw new IllegalStateException("asprof executable not found or not executable on : " + asProf.getAbsolutePath());
        }
        File soLib = new File(apDir, "lib" + File.separator + "libasyncProfiler.so");
        if (!soLib.exists() || !soLib.isFile()) {
            throw new IllegalStateException("libasyncProfiler.so not found on : " + soLib.getAbsolutePath());
        }
        return Optional.of(new AsyncProfiler(soLib, asProf, getAsyncProfilerAgentConfig()));
    }

    private static String getAsyncProfilerAgentConfig() {
        String apConfig = System.getenv().get("ASYNC_PROFILER_AGENT_CONFIG");
        if (StringUtils.isNotBlank(apConfig)) {
            return apConfig;
        }
        apConfig = System.getProperty("ASYNC_PROFILER_AGENT_CONFIG");
        if (StringUtils.isNotBlank(apConfig)) {
            return apConfig;
        }
        return "start,event=cpu,interval=1000000";
    }

    private static String getAsyncProfilerDir() {
        String apLibDir = System.getenv().get("ASYNC_PROFILER_DIR");
        if (StringUtils.isNotBlank(apLibDir)) {
            return apLibDir;
        }
        apLibDir = System.getProperty("ASYNC_PROFILER_DIR");
        if (StringUtils.isNotBlank(apLibDir)) {
            return apLibDir;
        }
        return null;
    }
}
