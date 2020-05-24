package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.Apps;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.quarkus.ts.startstop.utils.Commands.cleanDirOrFile;
import static io.quarkus.ts.startstop.utils.Commands.getArtifactGeneBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getLocalMavenRepoDir;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;


public class ExecutionDetails {
    String testMethodName;
    final String testClassName;

    public ExecutionDetails(TestInfo testInfo) {
        testClassName = testInfo.getTestClass().get().getCanonicalName();
        testMethodName = testInfo.getTestMethod().get().getName();
    }

    File appBaseDir = new File(getArtifactGeneBaseDir());
    File appDir = new File(appBaseDir, Apps.GENERATED_SKELETON.dir);
    String logsDir = appBaseDir.getAbsolutePath() + File.separator + Apps.GENERATED_SKELETON.dir + "-logs";
    String repoDir = getLocalMavenRepoDir();

    File generatorLog = new File(logsDir + File.separator + "bom-artifact-generator.log");
    File buildLog = new File(logsDir + File.separator + "bom-artifact-build.log");
    File runLog = new File(logsDir + File.separator + "bom-artifact-run.log");

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
        LOGGER.info(testMethodName + ": Generator command " + String.join(" ", generatorCmd));

        appendln(whatIDidReport, "# " + testClassName + ", " + testMethodName);
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

    public void runCommand(Runnable task) throws InterruptedException {
        ExecutorService buildService = Executors.newFixedThreadPool(1);
        buildService.submit(task);
        buildService.shutdown();
        buildService.awaitTermination(30, TimeUnit.MINUTES);
    }
}
