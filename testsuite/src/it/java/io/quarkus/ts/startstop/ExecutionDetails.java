package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.LogBuilder;
import io.quarkus.ts.startstop.utils.Logs;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.quarkus.ts.startstop.utils.Commands.cleanDirOrFile;
import static io.quarkus.ts.startstop.utils.Commands.getArtifactGeneBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getLocalMavenRepoDir;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.getLogsDir;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;


public class ExecutionDetails {
    String testMethodName;
    String testClassName;
    boolean warmUp = false;

    public ExecutionDetails(TestInfo testInfo) {
        this(testInfo, false);
    }
    public ExecutionDetails(TestInfo testInfo, boolean warmUp) {
        testClassName = testInfo.getTestClass().get().getCanonicalName();
        testMethodName = testInfo.getTestMethod().get().getName();
        this.warmUp = warmUp;
    }

    File appBaseDir = new File(getArtifactGeneBaseDir());
    File appDir = new File(appBaseDir, Apps.GENERATED_SKELETON.dir);
    String logsDir = appBaseDir.getAbsolutePath() + File.separator + Apps.GENERATED_SKELETON.dir + "-logs";
    String repoDir = getLocalMavenRepoDir();

    File generatorLog = new File(logsDir + File.separator + "artifact-generator.log");
    File buildLog = new File(logsDir + File.separator + "artifact-build.log");
    File runLog = new File(logsDir + File.separator + "artifact-run.log");

    StringBuilder whatIDidReport = new StringBuilder();

    public void prepareWorkspace() throws IOException {
        cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
        Files.createDirectories(Paths.get(logsDir));
        Files.createDirectories(Paths.get(repoDir));
    }
    public void archiveLogsAndCleanWorkspace() throws IOException {
        archiveLog(testClassName, testMethodName, generatorLog);
        if (buildLog.exists()) {
            archiveLog(testClassName, testMethodName, buildLog);
        }
        if (runLog.exists()) {
            archiveLog(testClassName, testMethodName, runLog);
        }
        writeReport(testClassName, testMethodName, whatIDidReport.toString());
        cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
    }

    public void reportGeneratorCmd(Logger LOGGER, List<String> generatorCmd) {
        LOGGER.info(testMethodName + (warmUp?": Warming up":"") + ": Generator command " + String.join(" ", generatorCmd));

        appendln(whatIDidReport, "# " + testClassName + ", " + testMethodName + (warmUp?", warm-up run":""));
        appendln(whatIDidReport, (new Date()).toString());
        appendln(whatIDidReport, appBaseDir.getAbsolutePath());
        appendlnSection(whatIDidReport, String.join(" ", generatorCmd));

    }
    public void reportBuildCmd(Logger LOGGER, List<String> buildCmd) {
        LOGGER.info(testMethodName + ": Build command " + String.join(" ", buildCmd));

        appendln(whatIDidReport, appDir.getAbsolutePath());
        appendlnSection(whatIDidReport, String.join(" ", buildCmd));
    }
    public void reportRunCmd(Logger LOGGER, List<String> runCmd) {
        LOGGER.info(testMethodName + ": Run command " + String.join(" ", runCmd));

        appendln(whatIDidReport, appDir.getAbsolutePath());
        appendlnSection(whatIDidReport, String.join(" ", runCmd));
    }

    public long runCommand(Runnable task) throws InterruptedException {
        ExecutorService buildService = Executors.newFixedThreadPool(1);
        buildService.submit(task);
        long buildStarts = System.currentTimeMillis();
        buildService.shutdown();
        buildService.awaitTermination(30, TimeUnit.MINUTES);
        long buildEnds = System.currentTimeMillis();
        return buildEnds - buildStarts;
    }

    public void editFileAndReport(Logger LOGGER) throws IOException {
        LOGGER.info("Testing reload...");
        Path srcFile = Paths.get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator +
                "org" + File.separator + "my" + File.separator + "group" + File.separator + "MyResource.java");
        appendlnSection(whatIDidReport, "Reloading class: " + srcFile.toAbsolutePath());
        try (Stream<String> src = Files.lines(srcFile)) {
            Files.write(srcFile, src.map(l -> l.replaceAll("hello", "bye")).collect(Collectors.toList()));
        }
    }

    public void measurements(LogBuilder.Log log) throws IOException {
        Path measurementsLog = Paths.get(getLogsDir(testClassName, testMethodName).toString(), "measurements.csv");
        Logs.logMeasurements(log, measurementsLog);
        appendln(whatIDidReport, "Measurements:");
        appendln(whatIDidReport, log.headerMarkdown + "\n" + log.lineMarkdown);

    }
}
