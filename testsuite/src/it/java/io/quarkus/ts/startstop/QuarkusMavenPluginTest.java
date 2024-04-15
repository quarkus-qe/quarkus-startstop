package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.Commands;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.quarkus.ts.startstop.utils.Commands.getBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.getQuarkusGroupId;
import static io.quarkus.ts.startstop.utils.Commands.getQuarkusVersion;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("generator")
public class QuarkusMavenPluginTest {

    private static final Logger LOGGER = Logger.getLogger(StartStopTest.class.getName());
    public static final String BASE_DIR = getBaseDir();

    public void runQuarkusMavenPluginGoal(TestInfo testInfo, Apps app, String goalName, String... expectedLogContent) throws IOException, InterruptedException {
        LOGGER.info("Testing app: " + app.toString() + ", goal name: " + goalName.toUpperCase());

        File logFile = null;
        StringBuilder whatIDidReport = new StringBuilder();
        File sourceDir = new File(BASE_DIR + File.separator + app.dir);
        File appDestDir = sourceDir
                .getParentFile().toPath().toAbsolutePath()
                .resolve("target")
                .resolve(goalName).toFile();

        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        try {
            // Create  a testdir
            if (!appDestDir.mkdirs()) {
                throw new RuntimeException("Cannot create directory " + appDestDir);
            }

            // Copy to path
            FileUtils.copyDirectory(sourceDir, appDestDir, true);
            LOGGER.info("Copying " + sourceDir +" to the " + appDestDir);
            // Cleanup
            Files.createDirectories(Paths.get(appDestDir.getAbsolutePath() + File.separator + "logs"));
            fixPom(appDestDir, goalName);
            // Run quarkus-maven-plugin goal
            logFile = new File(appDestDir.getAbsolutePath() + File.separator + "logs" + File.separator + goalName + ".log");
            ExecutorService buildService = Executors.newFixedThreadPool(1);

            List<String> baseBuildCmd = new ArrayList<>();
            baseBuildCmd.addAll(Arrays.asList("mvn", "clean", "quarkus:" + goalName));
            baseBuildCmd.add("-Dquarkus.version=" + getQuarkusVersion());
            baseBuildCmd.add("-Dquarkus.platform.group-id=" + getQuarkusGroupId());
            List<String> cmd = getBuildCommand(baseBuildCmd.toArray(new String[0]));

            LOGGER.info("Running " + String.join(" ",cmd) +" in " + appDestDir);

            buildService.submit(new Commands.ProcessRunner(appDestDir, logFile, cmd, 5));
            appendln(whatIDidReport, "# " + cn + ", " + mn);
            appendln(whatIDidReport, (new Date()).toString());
            appendln(whatIDidReport, appDestDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", cmd));
            buildService.shutdown();
            buildService.awaitTermination(5, TimeUnit.MINUTES);

            assertTrue(logFile.exists(), "Log file " + logFile + " doesn't exist");

            String logFileContent = Files.readString(logFile.toPath());
            assertTrue(logFileContent.contains("BUILD SUCCESS"), "Log file doesn't contain 'BUILD SUCCESS'");

            for (String content : expectedLogContent) {
                assertTrue(logFileContent.contains(content), "Log file doesn't contain '" + content + "'");
            }

        } finally {
            // Archive logs no matter what
            archiveLog(cn, mn, logFile);
            writeReport(cn, mn, whatIDidReport.toString());
        }
    }

    private static void fixPom(File workingDir, String name) throws IOException {
        Path path = workingDir.toPath().resolve("pom.xml");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        content = content
                .replaceAll("<relativePath>..</relativePath>", "<relativePath>../..</relativePath>")
                .replaceAll("app-jakarta-rest-minimal", "app-jakarta-rest-minimal-" + name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void helpTarget(TestInfo testInfo) throws IOException, InterruptedException {
        runQuarkusMavenPluginGoal(testInfo, Apps.JAKARTA_REST_MINIMAL, "help", "quarkus:info", "quarkus:update");
    }

    @Test
    public void infoTarget(TestInfo testInfo) throws IOException, InterruptedException {
        runQuarkusMavenPluginGoal(testInfo, Apps.JAKARTA_REST_MINIMAL, "info", "io.quarkus:quarkus-rest");
    }

    @Test
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/33403
    public void updateTarget(TestInfo testInfo) throws IOException, InterruptedException {
        runQuarkusMavenPluginGoal(testInfo, Apps.JAKARTA_REST_MINIMAL, "update", "quarkus:update goal is experimental");
    }

}
