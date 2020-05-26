package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.LogBuilder;
import io.quarkus.ts.startstop.utils.Logs;
import io.quarkus.ts.startstop.utils.MvnCmds;
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

import static io.quarkus.ts.startstop.utils.Commands.cleanTarget;
import static io.quarkus.ts.startstop.utils.Commands.getBaseDir;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.getLogsDir;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;

public class ExecutionDetailsForStartStop {

    public ExecutionDetailsForStartStop(TestInfo testInfo, Apps app, MvnCmds mvnCmds) {
        this.app = app;
        this.mvnCmds = mvnCmds;
        this.testClassName = testInfo.getTestClass().get().getCanonicalName();
        this.testMethodName = testInfo.getTestMethod().get().getName();

        appDir = new File(getBaseDir(), app.dir);
        logsDir = appDir.getAbsolutePath() + File.separator + "logs";
        buildLog = new File(logsDir + File.separator + mvnCmds.name().toLowerCase() + "-build.log");
        runLog = new File(logsDir + File.separator + mvnCmds.name().toLowerCase() + "-run.log");
    }

    final Apps app;
    final MvnCmds mvnCmds;
    final String testMethodName;
    final String testClassName;

    final StringBuilder whatIDidReport = new StringBuilder();
    final File appDir;
    final String logsDir;
    final File buildLog;
    final File runLog;

    public void prepareWorkspace() throws IOException {
        cleanTarget(app);
        Files.createDirectories(Paths.get(logsDir));
    }
    public void archiveLogsAndCleanWorkspace() throws IOException {
        archiveLog(testClassName, testMethodName, buildLog);
        archiveLog(testClassName, testMethodName, runLog);

        writeReport(testClassName, testMethodName, whatIDidReport.toString());
        cleanTarget(app);
    }

    public void reportBuildCmd(Logger LOGGER, List<String> buildCmd) {
        LOGGER.info("Testing app: " + app.toString() + ", mode: " + mvnCmds.toString());

        appendln(whatIDidReport, "# " + testClassName + ", " + testMethodName);
        appendln(whatIDidReport, (new Date()).toString());
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

    public void measurements(LogBuilder.Log log) throws IOException {
        Path measurementsLog = Paths.get(getLogsDir(testClassName, testMethodName).toString(), "measurements.csv");
        Logs.logMeasurements(log, measurementsLog);
        appendln(whatIDidReport, "Measurements:");
        appendln(whatIDidReport, log.headerMarkdown + "\n" + log.lineMarkdown);
    }
}
