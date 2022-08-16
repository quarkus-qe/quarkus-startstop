package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.Commands;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.quarkus.ts.startstop.utils.Commands.getQuarkusGroupId;
import static io.quarkus.ts.startstop.utils.Commands.getQuarkusVersion;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.cleanTarget;
import static io.quarkus.ts.startstop.utils.Commands.getBaseDir;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Quarkus native mode with "-Dquarkus.native.debug.enabled=true"
 * Verify that debug symbols are created as well as the source cache
 */
@Tag("native")
@EnabledOnOs({OS.LINUX}) // debug details are available only on Linux
public class NativeDebugTest {

    private final Apps app = Apps.JAX_RS_MINIMAL;
    private final MvnCmds mvnCmds = MvnCmds.NATIVE;

    private static final Logger LOGGER = Logger.getLogger(StartStopTest.class.getName());
    public static final String BASE_DIR = getBaseDir();

    public void testRuntime(TestInfo testInfo, File[] searchFiles) throws IOException, InterruptedException {
        LOGGER.info("Testing app: " + app.toString() + ", mode: " + mvnCmds.toString());

        File buildLogA = null;
        File appDir = new File(BASE_DIR + File.separator + app.dir);
        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        try {
            // Cleanup
            cleanTarget(app);
            Files.createDirectories(Paths.get(appDir.getAbsolutePath() + File.separator + "logs"));

            // Build
            buildLogA = new File(appDir.getAbsolutePath() + File.separator + "logs" + File.separator + mvnCmds.name().toLowerCase() + "-build.log");
            ExecutorService buildService = Executors.newFixedThreadPool(1);

            List<String> baseBuildCmd = new ArrayList<>();
            baseBuildCmd.addAll(Arrays.asList(mvnCmds.mvnCmds[0]));
            baseBuildCmd.add("-Dquarkus.version=" + getQuarkusVersion());
            baseBuildCmd.add("-Dquarkus.platform.group-id=" + getQuarkusGroupId());
            baseBuildCmd.add("-Dquarkus.native.debug.enabled=true");
            List<String> cmd = getBuildCommand(baseBuildCmd.toArray(new String[0]));

            LOGGER.info("Building (" + cmd + ")");
            buildService.submit(new Commands.ProcessRunner(appDir, buildLogA, cmd, 20));
            buildService.shutdown();
            buildService.awaitTermination(30, TimeUnit.MINUTES);

            for (File searchFile : searchFiles) {
                assertTrue(searchFile.exists(), "File or directory: " + searchFile.getAbsolutePath() + " is missing.");
            }
            assertTrue(buildLogA.exists());
            checkLog(cn, mn, app, mvnCmds, buildLogA);
        } finally {
            // Archive logs no matter what
            archiveLog(cn, mn, buildLogA);
            cleanTarget(app);
        }
    }

    @Test
    public void debugSymbolsCheck(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, new File[]{
                new File(BASE_DIR + File.separator + app.dir + File.separator + "target" + File.separator + "quarkus-runner.debug"),
                new File(BASE_DIR + File.separator + app.dir + File.separator + "target" + File.separator + "sources")});
    }
}
