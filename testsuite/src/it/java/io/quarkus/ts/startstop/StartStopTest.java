package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.AsyncProfiler;
import io.quarkus.ts.startstop.utils.Commands;
import io.quarkus.ts.startstop.utils.LogBuilder;
import io.quarkus.ts.startstop.utils.Logs;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.OpenTelemetryCollector;
import io.quarkus.ts.startstop.utils.UnitTestResource;
import io.quarkus.ts.startstop.utils.WebpageTester;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.quarkus.ts.startstop.utils.Commands.cleanTarget;
import static io.quarkus.ts.startstop.utils.Commands.dropCaches;
import static io.quarkus.ts.startstop.utils.Commands.getBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.getOpenedFDs;
import static io.quarkus.ts.startstop.utils.Commands.getQuarkusGroupId;
import static io.quarkus.ts.startstop.utils.Commands.getQuarkusVersion;
import static io.quarkus.ts.startstop.utils.Commands.getRSSkB;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Commands.disableCleanup;
import static io.quarkus.ts.startstop.utils.Logs.SKIP;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkListeningHost;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.checkThreshold;
import static io.quarkus.ts.startstop.utils.Logs.getLogsDir;
import static io.quarkus.ts.startstop.utils.Logs.parseStartStopTimestamps;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static io.quarkus.ts.startstop.utils.RunCommandAugmentor.setCommandPrefix;
import static io.quarkus.ts.startstop.utils.RunCommandAugmentor.setMemoryLimits;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for build and start of applications with some real source code
 * Detains in https://github.com/quarkus-qe/quarkus-startstop#startstoptest
 */
@Tag("startstop")
public class StartStopTest {

    private static final Logger LOGGER = Logger.getLogger(StartStopTest.class.getName());

    public static final String BASE_DIR = getBaseDir();

    public void testRuntime(TestInfo testInfo, Apps app, MvnCmds mvnCmds) throws IOException, InterruptedException {
        testRuntime(testInfo, app, mvnCmds, UnitTestResource.NOOP_SUPPLIER);
    }

    public void testRuntime(TestInfo testInfo, Apps app, MvnCmds mvnCmds, Supplier<UnitTestResource> testResourceSupplier) throws IOException, InterruptedException {
        LOGGER.info("Testing app: " + app.toString() + ", mode: " + mvnCmds.toString());
        LOGGER.info("Cleanup Enabled: " + !disableCleanup());

        Process pA = null;
        File buildLogA = null;
        File runLogA = null;
        StringBuilder whatIDidReport = new StringBuilder();
        File appDir = new File(BASE_DIR + File.separator + app.dir);
        Optional<AsyncProfiler> asyncProfiler = mvnCmds == MvnCmds.JVM ? AsyncProfiler.create() : Optional.empty();
        String canonicalName = testInfo.getTestClass().get().getCanonicalName();
        String methodName = testInfo.getTestMethod().get().getName();
        try (var testResource = testResourceSupplier.get()) {
            // Cleanup
            asyncProfiler.ifPresent(ignore -> AsyncProfiler.cleanProfilingResults(app));
            cleanTarget(app);
            Files.createDirectories(Paths.get(appDir.getAbsolutePath() + File.separator + "logs"));

            // Build
            buildLogA = new File(appDir.getAbsolutePath() + File.separator + "logs" + File.separator + mvnCmds.name().toLowerCase() + "-build.log");
            ExecutorService buildService = Executors.newFixedThreadPool(1);

            List<String> baseBuildCmd = new ArrayList<>();
            baseBuildCmd.addAll(Arrays.asList(mvnCmds.mvnCmds[0]));
            baseBuildCmd.add("-Dquarkus.version=" + getQuarkusVersion());
            baseBuildCmd.add("-Dquarkus.platform.group-id=" + getQuarkusGroupId());
            final List<String> buildCommand = getBuildCommand(baseBuildCmd.toArray(new String[0]));
            LOGGER.info("Running " + baseBuildCmd + " in the " + appDir.getAbsolutePath());

            buildService.submit(new Commands.ProcessRunner(appDir, buildLogA, buildCommand, 20));
            appendln(whatIDidReport, "# " + canonicalName + ", " + methodName);
            appendln(whatIDidReport, (new Date()).toString());
            appendln(whatIDidReport, appDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", buildCommand));
            long buildStarts = System.currentTimeMillis();
            buildService.shutdown();
            buildService.awaitTermination(30, TimeUnit.MINUTES);
            long buildEnds = System.currentTimeMillis();

            assertTrue(buildLogA.exists());
            boolean skipLogCheck = Boolean.getBoolean("start-stop.skip.log-check");
            if (!skipLogCheck) {
                checkLog(canonicalName, methodName, app, mvnCmds, buildLogA);
            }

            boolean isNative = mvnCmds == MvnCmds.NATIVE;
            if (isNative) {
                String nativeBinaryLocation = mvnCmds.mvnCmds[1][0];
                Path nativeBinaryPath = Paths.get(appDir.getAbsolutePath(), nativeBinaryLocation);
                appendln(whatIDidReport, "Native binary path: " + nativeBinaryPath);
                long bytes = Files.size(nativeBinaryPath);
                String prettySize = FileUtils.byteCountToDisplaySize(bytes);
                LOGGER.info("Native binary SIZE: " + prettySize);
                appendlnSection(whatIDidReport, "   SIZE: " + prettySize);
            }

            List<Long> rssKbList = new ArrayList<>(10);
            List<Long> timeToFirstOKRequestList = new ArrayList<>(10);
            int iterations = Integer.getInteger("start-stop.iterations", 10);
            boolean coldStart = Boolean.getBoolean("start-stop.cold-start");
            boolean skipThresholdCheck = Boolean.getBoolean("start-stop.skip.threshold-check");
            List<String> commandPrefix = getSystemPropertyAsList("start-stop.command.prefix", "");
            List<String> jvmMemory = getSystemPropertyAsList("start-stop.jvm.memory", "-Xmx256m");
            List<String> nativeMemory = getSystemPropertyAsList("start-stop.native.memory", "-Xmx96m");
            for (int i = 0; i < iterations; i++) {
                // Run
                LOGGER.info("Running... round " + i);
                runLogA = new File(appDir.getAbsolutePath() + File.separator + "logs" + File.separator + mvnCmds.name().toLowerCase() + "-run.log");
                appendln(whatIDidReport, appDir.getAbsolutePath());
                var runCommand = asyncProfiler.map(control -> control.createJavaProfiledRunCommand(mvnCmds.mvnCmds[1]))
                        .orElseGet(() -> getRunCommand(mvnCmds.mvnCmds[1]));

                runCommand = setCommandPrefix(runCommand, commandPrefix);
                runCommand = setMemoryLimits(runCommand, isNative ? nativeMemory:jvmMemory, isNative);

                appendlnSection(whatIDidReport, String.join(" ", runCommand));
                if (coldStart) {
                    LOGGER.info("Using COLD start");
                    dropCaches();
                }
                pA = runCommand(runCommand, appDir, runLogA);

                // Test web pages
                long timeToFirstOKRequest = WebpageTester.testWeb(app.urlContent.urlContent[0][0], 10, app.urlContent.urlContent[0][1], true);

                final Process currentProcess = pA;
                final int runId = i;
                asyncProfiler.ifPresent(control -> control.stopProfing(appDir, mvnCmds, currentProcess, runId));

                LOGGER.info("Testing web page content...");
                for (String[] urlContent : app.urlContent.urlContent) {
                    WebpageTester.testWeb(urlContent[0], 5, urlContent[1], false);
                }

                LOGGER.info("Terminate and scan logs...");
                pA.getInputStream().available();

                long rssKb = getRSSkB(pA.pid());
                long openedFiles = getOpenedFDs(pA.pid());

                processStopper(pA, false);

                LOGGER.info("Gonna wait for ports closed...");
                // Release ports
                assertTrue(waitForTcpClosed("localhost", parsePort(app.urlContent.urlContent[0][0]), 60),
                        "Main port is still open");
                if (!skipLogCheck) {
                    checkLog(canonicalName, methodName, app, mvnCmds, runLogA);
                }
                checkListeningHost(canonicalName, methodName, mvnCmds, runLogA);

                if (commandPrefix.size() > 0) {
                    // unfortunately some active wait is needed to get stop message into logs
                    Thread.sleep(1000l);
                }
                float[] startedStopped = parseStartStopTimestamps(runLogA);

                Path measurementsLog = Paths.get(getLogsDir(canonicalName, methodName).toString(), "measurements.csv");
                LogBuilder.Log log = new LogBuilder()
                        .app(app)
                        .mode(mvnCmds)
                        .buildTimeMs(buildEnds - buildStarts)
                        .timeToFirstOKRequestMs(timeToFirstOKRequest)
                        .startedInMs((long) (startedStopped[0] * 1000))
                        .stoppedInMs((long) (startedStopped[1] * 1000))
                        .rssKb(rssKb)
                        .openedFiles(openedFiles)
                        .build();
                Logs.logMeasurements(log, measurementsLog);
                appendln(whatIDidReport, "Measurements:");
                appendln(whatIDidReport, log.headerMarkdown + "\n" + log.lineMarkdown);

                rssKbList.add(rssKb);
                timeToFirstOKRequestList.add(timeToFirstOKRequest);

                testResource.reset();
            }
            LOGGER.info("Calculating the stats");
            long rssKbAvgWithoutMinMax = getAvgWithoutMinMax(rssKbList);
            long timeToFirstOKRequestAvgWithoutMinMax = getAvgWithoutMinMax(timeToFirstOKRequestList);
            LOGGER.info("AVG timeToFirstOKRequest (ms) without min and max values: " + timeToFirstOKRequestAvgWithoutMinMax);
            LOGGER.info("AVG RSS (kB) without min and max values: " + rssKbAvgWithoutMinMax);
            if (!skipThresholdCheck) {
                checkThreshold(app, mvnCmds, rssKbAvgWithoutMinMax, timeToFirstOKRequestAvgWithoutMinMax, SKIP);
            }
        } finally {
            // Make sure processes are down even if there was an exception / failure
            if (pA != null) {
                processStopper(pA, true);
            }
            asyncProfiler.ifPresent(profiler -> profiler.archiveProfilingResults(canonicalName, methodName, appDir));
            archiveLog(canonicalName, methodName, buildLogA);
            archiveLog(canonicalName, methodName, runLogA);
            writeReport(canonicalName, methodName, whatIDidReport.toString());
            if (!disableCleanup()) {
                cleanTarget(app);
            }
        }
    }

    private static List<String> getSystemPropertyAsList(String key, String def) {
        String memoryString = System.getProperty(key, def);
        return Arrays.asList(memoryString.split(" "));
    }

    private long getAvgWithoutMinMax(List<Long> listOfValues) {
        listOfValues.remove(Collections.min(listOfValues));
        listOfValues.remove(Collections.max(listOfValues));
        return (long) listOfValues.stream().mapToLong(val -> val).average().orElse(Long.MAX_VALUE);
    }

    @Test
    public void jakartaRESTMinimalJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAKARTA_REST_MINIMAL, MvnCmds.JVM);
    }

    @Test
    @Tag("native")
    public void jakartaRESTMinimalNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAKARTA_REST_MINIMAL, MvnCmds.NATIVE);
    }

    @Test
    public void fullMicroProfileJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.FULL_MICROPROFILE, MvnCmds.JVM, OpenTelemetryCollector::new);
    }

    @Test
    @Tag("native")
    public void fullMicroProfileNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.FULL_MICROPROFILE, MvnCmds.NATIVE, OpenTelemetryCollector::new);
    }
}
