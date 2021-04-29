package io.quarkus.ts.startstop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.Commands;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.WebpageTester;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static io.quarkus.ts.startstop.utils.Commands.cleanTarget;
import static io.quarkus.ts.startstop.utils.Commands.getBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.getQuarkusVersion;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for running Quarkus applications on path with special characters
 *
 * @author Ondrej Machala <omachala@redhat.com>
 */
@Tag("special-chars")
public class SpecialCharsTest {
    private static final Logger LOGGER = Logger.getLogger(SpecialCharsTest.class.getName());

    public static final String BASE_DIR = getBaseDir();

    public void testRuntime(TestInfo testInfo, Apps app, MvnCmds mvnCmds, String subdir) throws IOException, InterruptedException {
        Process pA = null;
        File buildLogA = null;
        File runLogA = null;
        StringBuilder whatIDidReport = new StringBuilder();
        File appBaseDir = new File(BASE_DIR, app.dir);
        File appDestDir = new File(appBaseDir.getParentFile(), subdir);
        File appDir = new File(appDestDir, app.dir);
        File appPomXml = new File(appDir, "pom.xml");
        File logsDir = new File(appDir, "special-chars-logs");
        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        LOGGER.info("Testing app: " + app.toString() + ", mode: " + mvnCmds.toString() + ", on path " + appDestDir);
        try {
            // Clean target directory
            cleanTarget(app);

            removeDirWithSpecialCharacters(appDestDir);

            // Make dir with special chars
            if (!appDestDir.mkdir()) {
                throw new RuntimeException("Cannot create directory " + appDestDir);
            }

            // Copy to path with special characters
            FileUtils.copyDirectoryToDirectory(appBaseDir, appDestDir);

            // Replace relative path to parent project
            Path path = appPomXml.toPath();
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            content = content.replaceAll("<relativePath>..</relativePath>", "<relativePath>../..</relativePath>");
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));

            // Create logs directory
            Files.createDirectories(logsDir.toPath());

            List<String> cmd;
            // Build
            if (mvnCmds != MvnCmds.DEV) {
                buildLogA = new File(logsDir + File.separator + subdir + "-" + mvnCmds.name().toLowerCase() + "-build.log");
                ExecutorService buildService = Executors.newFixedThreadPool(1);
                List<String> baseBuildCmd = new ArrayList<>();
                baseBuildCmd.addAll(Arrays.asList(mvnCmds.mvnCmds[0]));
                baseBuildCmd.add("-Dquarkus.version=" + getQuarkusVersion());
                cmd = getBuildCommand(baseBuildCmd.toArray(new String[0]));

                appendln(whatIDidReport, "# " + cn + ", " + mn);
                appendln(whatIDidReport, (new Date()).toString());
                appendln(whatIDidReport, appDir.getAbsolutePath());
                appendlnSection(whatIDidReport, String.join(" ", cmd));

                LOGGER.info("Building (" + cmd + ")");
                buildService.submit(new Commands.ProcessRunner(appDir, buildLogA, cmd, 20));

                buildService.shutdown();
                buildService.awaitTermination(30, TimeUnit.MINUTES);

                assertTrue(buildLogA.exists());
                checkLog(cn, mn, app, mvnCmds, buildLogA);
            }

            // Run
            runLogA = new File(logsDir + File.separator + subdir +  "-" + mvnCmds.name().toLowerCase() + "-run.log");

            if (mvnCmds == MvnCmds.DEV) {
                List<String> baseBuildCmd = new ArrayList<>();
                baseBuildCmd.addAll(Arrays.asList(mvnCmds.mvnCmds[0]));
                baseBuildCmd.add("-Dquarkus.version=" + getQuarkusVersion());
                cmd = getRunCommand(baseBuildCmd.toArray(new String[0]));
            } else {
                cmd = getRunCommand(mvnCmds.mvnCmds[1]);
            }
            LOGGER.info("Running (" + cmd + ")");
            appendln(whatIDidReport, appDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", cmd));
            pA = runCommand(cmd, appDir, runLogA);

            // Test web page
            LOGGER.info("Testing web page content...");
            int timeout = mvnCmds != MvnCmds.DEV ? 5 : 60;
            for (String[] urlContent : app.urlContent.urlContent) {
                WebpageTester.testWeb(urlContent[0], timeout, urlContent[1], false);
            }

            processStopper(pA, false);
        } finally {
            // Make sure processes are down even if there was an exception / failure
            if (pA != null) {
                processStopper(pA, true);
                pA.waitFor();
            }

            // Archive logs
            if (buildLogA != null) {
                archiveLog(cn, mn, buildLogA);
            }
            if (runLogA != null) {
                archiveLog(cn, mn, runLogA);
            }
            writeReport(cn, mn, whatIDidReport.toString());

            removeDirWithSpecialCharacters(appDestDir);
        }
    }

    @Test
    public void spacesJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.JVM, "s p a c e s j v m");
    }

    @Test
    public void spacesDEV(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.DEV, "s p a c e s d e v");
    }

    @Test
    @Tag("native")
    public void spacesNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.NATIVE, "s p a c e s n a t i v e");
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/15358")
    public void specialJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.JVM, ",;~!@#$%^&()");
    }

    @Test
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/9707
    public void specialDEV(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.DEV, ",;~!@#$%^&()");
    }

    @Test
    @Tag("native")
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/9707
    public void specialNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.NATIVE, ",;~!@#$%^&()");
    }

    @Test
    public void diacriticsJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.JVM, "ěščřžýáíéůú");
    }

    @Test
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/9707
    public void diacriticsDEV(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.DEV, "ěščřžýáíéůú");
    }

    @Test
    @Tag("native")
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/9707
    public void diacriticsNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.NATIVE, "ěščřžýáíéůú");
    }

    @Test
    public void japaneseJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.JVM, "元気かい");
    }

    @Test
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/9707
    public void japaneseDEV(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.DEV, "元気かい");
    }

    @Test
    @Tag("native")
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/9707
    public void japaneseNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.NATIVE, "元気かい");
    }

    @Test
    public void otherJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.JVM, "Îñţérñåţîöñåļîžåţîờñ");
    }

    @Test
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/9707
    public void otherDEV(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.DEV, "Îñţérñåţîöñåļîžåţîờñ");
    }

    @Test
    @Tag("native")
    @DisabledOnOs({OS.WINDOWS}) // https://github.com/quarkusio/quarkus/issues/9707
    public void otherNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.NATIVE, "Îñţérñåţîöñåļîžåţîờñ");
    }

    private void removeDirWithSpecialCharacters(File appDestDir) {
        // Remove dir with special chars
        try {
            if (appDestDir.exists()) {
                FileUtils.deleteDirectory(appDestDir);
            }
        } catch (IOException ignored) {
            // ignored when the folder could not be deleted.
        }
    }

}
