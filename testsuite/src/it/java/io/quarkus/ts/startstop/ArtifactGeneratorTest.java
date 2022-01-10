package io.quarkus.ts.startstop;

import static io.quarkus.ts.startstop.utils.Commands.adjustPrettyPrintForJsonLogging;
import static io.quarkus.ts.startstop.utils.Commands.cleanDirOrFile;
import static io.quarkus.ts.startstop.utils.Commands.confAppPropsForSkeleton;
import static io.quarkus.ts.startstop.utils.Commands.copyFileForSkeleton;
import static io.quarkus.ts.startstop.utils.Commands.getArtifactGeneBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getGeneratorCommand;
import static io.quarkus.ts.startstop.utils.Commands.getOpenedFDs;
import static io.quarkus.ts.startstop.utils.Commands.getRSSkB;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Logs.SKIP;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.checkThreshold;
import static io.quarkus.ts.startstop.utils.Logs.getLogsDir;
import static io.quarkus.ts.startstop.utils.Logs.parseStartStopTimestamps;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.Commands;
import io.quarkus.ts.startstop.utils.LogBuilder;
import io.quarkus.ts.startstop.utils.Logs;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.TestFlags;
import io.quarkus.ts.startstop.utils.URLContent;
import io.quarkus.ts.startstop.utils.WebpageTester;

/**
 * Tests for quarkus-maven-plugin generator
 */
@Tag("generator")
public class ArtifactGeneratorTest {

    private static final Logger LOGGER = Logger.getLogger(ArtifactGeneratorTest.class.getName());

    public static final String[] supportedExtensionsSubsetSetA = new String[]{
            "agroal",
            "config-yaml",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
            "hibernate-validator",
            "jackson",
            "jaxb",
            "jdbc-mysql",
            "jdbc-postgresql",
            "jsonb",
            "jsonp",
            "kafka-client",
            "logging-json",
            "narayana-jta",
            "oidc",
            "quarkus-quartz", // "quartz" is ambiguous with org.apache.camel.quarkus:camel-quarkus-quartz
            "reactive-pg-client",
            "rest-client",
            "rest-client-jaxb",
            "resteasy",
            "resteasy-jackson",
            "resteasy-jaxb",
            "resteasy-jsonb",
            "scheduler",
            "spring-boot-properties",
            "smallrye-reactive-streams-operators",
            "spring-data-jpa",
            "spring-di",
            "spring-security",
            "spring-web",
            "undertow",
            "vertx",
            "vertx-web",
            "grpc",
            "infinispan-client",
            "cache",
            "micrometer",
            "micrometer-registry-prometheus",
            "quarkus-openshift-client",
    };

    public static final String[] supportedExtensionsSubsetSetB = new String[]{
            "agroal",
            "quarkus-avro",
            "config-yaml",
            "container-image-openshift",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
            "hibernate-validator",
            "rest-client",
            "rest-client-jackson",
            "jackson",
            "jaxb",
            "jdbc-mariadb",
            "jdbc-mssql",
            "mutiny",
            "oidc-client",
            "oidc-client-filter",
            "reactive-mysql-client",
            "resteasy-multipart",
            "smallrye-context-propagation",
            "smallrye-fault-tolerance",
            "smallrye-health",
            "smallrye-jwt",
            "smallrye-jwt-build",
            "smallrye-metrics",
            "smallrye-openapi",
            "smallrye-opentracing",
            "smallrye-reactive-messaging",
            "smallrye-reactive-messaging-amqp",
            "smallrye-reactive-messaging-kafka",
            "spring-data-jpa",
            "spring-data-rest",
            "spring-di",
            "spring-security",
            "spring-web",
            "spring-cloud-config-client",
            "spring-scheduled",
            "spring-cache",
            "websockets",
            "websockets-client",
    };

    public static final String[] supportedReactiveExtensionsSubsetSetA = new String[]{
            "quarkus-jaxrs-client-reactive",
            "quarkus-resteasy-reactive",
            "quarkus-resteasy-reactive-jackson",
            "quarkus-rest-client-reactive",
            "quarkus-rest-client-reactive-jackson",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
            "hibernate-validator",
            "jackson",
            "jaxb",
            "jdbc-mysql",
            "jdbc-postgresql",
            "jsonb",
            "jsonp",
            "kafka-client",
            "logging-json",
            "narayana-jta",
            "oidc",
            "quarkus-quartz",
            "reactive-pg-client",
            "scheduler",
            "spring-boot-properties",
            "smallrye-reactive-streams-operators",
            "spring-data-jpa",
            "spring-di",
            "spring-security",
            // problematic with reactive - https://issues.redhat.com/browse/QUARKUS-1300
//            "spring-web",
            // https://github.com/quarkusio/quarkus/issues/20302
//            "undertow",
            "vertx",
            "vertx-web",
            "grpc",
            "infinispan-client",
            "cache",
            "micrometer",
            "micrometer-registry-prometheus",
            "quarkus-openshift-client",
    };

    public static final String[] supportedReactiveExtensionsSubsetSetB = new String[]{
            "quarkus-jaxrs-client-reactive",
            "quarkus-resteasy-reactive",
            "quarkus-resteasy-reactive-jsonb",
            "quarkus-rest-client-reactive",
            "agroal",
            "quarkus-avro",
            "config-yaml",
            "container-image-openshift",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
            "hibernate-validator",
            "jackson",
            "jaxb",
            "jdbc-mariadb",
            "jdbc-mssql",
            "mutiny",
            "oidc-client",
            // problematic with reactive - https://issues.redhat.com/browse/QUARKUS-1300
//            "oidc-client-filter",
            "reactive-mysql-client",
            "smallrye-context-propagation",
            "smallrye-fault-tolerance",
            "smallrye-health",
            "smallrye-jwt",
            "smallrye-jwt-build",
            "smallrye-metrics",
            "smallrye-openapi",
            "smallrye-opentracing",
            "smallrye-reactive-messaging",
            "smallrye-reactive-messaging-amqp",
            "smallrye-reactive-messaging-kafka",
            "spring-data-jpa",
            // problematic with reactive - https://issues.redhat.com/browse/QUARKUS-1300
//            "spring-data-rest",
            "spring-di",
            "spring-security",
            // problematic with reactive - https://issues.redhat.com/browse/QUARKUS-1300
//            "spring-web",
            "spring-cloud-config-client",
            "spring-scheduled",
            "spring-cache",
            // TODO https://github.com/quarkusio/quarkus/issues/18843 / https://issues.redhat.com/browse/QUARKUS-1291
//             "websockets",
//             "websockets-client",
    };

    public void testRuntime(TestInfo testInfo, String[] extensions, Set<TestFlags> flags) throws Exception {
        Process pA = null;
        File buildLogA = null;
        File runLogA = null;
        StringBuilder whatIDidReport = new StringBuilder();
        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        File appBaseDir = new File(getArtifactGeneBaseDir());
        File appDir = new File(appBaseDir, Apps.GENERATED_SKELETON.dir);
        String logsDir = appBaseDir.getAbsolutePath() + File.separator + Apps.GENERATED_SKELETON.dir + "-logs";
        List<String> generatorCmd = getGeneratorCommand(MvnCmds.GENERATOR.mvnCmds[0], extensions);
        List<String> runCmd = getRunCommand(MvnCmds.DEV.mvnCmds[0]);
        URLContent skeletonApp = Apps.GENERATED_SKELETON.urlContent;
        if (flags.contains(TestFlags.WARM_UP)) {
            LOGGER.info(mn + ": Warming up setup: " + String.join(" ", generatorCmd));
        } else {
            LOGGER.info(mn + ": Testing setup: " + String.join(" ", generatorCmd));
        }

        try {
            // Cleanup
            cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
            Files.createDirectories(Paths.get(logsDir));

            // Build
            buildLogA = new File(logsDir + File.separator + (flags.contains(TestFlags.WARM_UP) ? "warmup-artifact-build.log" : "artifact-build.log"));
            ExecutorService buildService = Executors.newFixedThreadPool(1);
            buildService.submit(new Commands.ProcessRunner(appBaseDir, buildLogA, generatorCmd, 20));
            appendln(whatIDidReport, "# " + cn + ", " + mn + ", warmup run: " + flags.contains(TestFlags.WARM_UP));
            appendln(whatIDidReport, (new Date()).toString());
            appendln(whatIDidReport, appBaseDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", generatorCmd));
            long buildStarts = System.currentTimeMillis();
            buildService.shutdown();
            buildService.awaitTermination(30, TimeUnit.MINUTES);
            long buildEnds = System.currentTimeMillis();

            assertTrue(buildLogA.exists());
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, buildLogA);

            // Config, see app-generated-skeleton/README.md
            confAppPropsForSkeleton(appDir.getAbsolutePath());
            adjustPrettyPrintForJsonLogging(appDir.getAbsolutePath());

            // Run
            LOGGER.info("Running... " + runCmd);
            runLogA = new File(logsDir + File.separator + (flags.contains(TestFlags.WARM_UP) ? "warmup-dev-run.log" : "dev-run.log"));
            appendln(whatIDidReport, appDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", runCmd));
            pA = runCommand(runCmd, appDir, runLogA);
            // Test web pages
            // The reason for a seemingly large timeout of 20 minutes is that dev mode will be downloading the Internet on the first fresh run.
            long timeoutS = (flags.contains(TestFlags.WARM_UP) ? 20 * 60 : 60);
            long timeToFirstOKRequest = WebpageTester.testWeb(skeletonApp.urlContent[0][0], timeoutS,
                    skeletonApp.urlContent[0][1], !flags.contains(TestFlags.WARM_UP));

            if (flags.contains(TestFlags.WARM_UP)) {
                LOGGER.info("Terminating warmup and scanning logs...");
                pA.getInputStream().available();
                processStopper(pA, false);
                LOGGER.info("Gonna wait for ports closed after warmup...");
                // Release ports
                assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                        "Main port is still open after warmup");
                checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, runLogA);
                return;
            }

            LOGGER.info("Testing reload...");
            // modify existing class
            Path srcFile = Paths.get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator +
                    "org" + File.separator + "my" + File.separator + "group" + File.separator +
                    (flags.contains(TestFlags.RESTEASY_REACTIVE)?"ReactiveGreetingResource.java":"GreetingController.java"));
            appendlnSection(whatIDidReport, "Reloading class: " + srcFile.toAbsolutePath());
            try (Stream<String> src = Files.lines(srcFile)) {
                Files.write(srcFile, src.map(l -> l.replaceAll("Hello", "Bye")).collect(Collectors.toList()));
            }

            // test modified class and measure time
            long timeToReloadedOKRequest = WebpageTester.testWeb(
                    flags.contains(TestFlags.RESTEASY_REACTIVE)?skeletonApp.urlContent[3][0]:skeletonApp.urlContent[1][0], 60,
                    flags.contains(TestFlags.RESTEASY_REACTIVE)?skeletonApp.urlContent[3][1]:skeletonApp.urlContent[1][1], true);

            // add new class
            Path addedFile = Paths
                    .get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator +
                            "org" + File.separator + "my" + File.separator + "group" + File.separator + "AddedController.java");
            appendlnSection(whatIDidReport, "Adding class: " + addedFile.toAbsolutePath());
            copyFileForSkeleton("AddedController.java", addedFile);

            // test added class
            WebpageTester.testWeb(skeletonApp.urlContent[2][0], 60, skeletonApp.urlContent[2][1], false);

            LOGGER.info("Terminate and scan logs...");
            pA.getInputStream().available();

            long rssKb = getRSSkB(pA.pid());
            long openedFiles = getOpenedFDs(pA.pid());

            processStopper(pA, false);

            LOGGER.info("Gonna wait for ports closed...");
            // Release ports
            assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                    "Main port is still open");
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, runLogA);

            float[] startedStopped = parseStartStopTimestamps(runLogA);

            Path measurementsLog = Paths.get(getLogsDir(cn, mn).toString(), "measurements.csv");
            LogBuilder.Log log = new LogBuilder()
                    .app(Apps.GENERATED_SKELETON)
                    .mode(MvnCmds.GENERATOR)
                    .buildTimeMs(buildEnds - buildStarts)
                    .timeToFirstOKRequestMs(timeToFirstOKRequest)
                    .timeToReloadedOKRequest(timeToReloadedOKRequest)
                    .startedInMs((long) (startedStopped[0] * 1000))
                    .stoppedInMs((long) (startedStopped[1] * 1000))
                    .rssKb(rssKb)
                    .openedFiles(openedFiles)
                    .build();
            Logs.logMeasurements(log, measurementsLog);
            appendln(whatIDidReport, "Measurements:");
            appendln(whatIDidReport, log.headerMarkdown + "\n" + log.lineMarkdown);
            checkThreshold(Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, SKIP, timeToFirstOKRequest, timeToReloadedOKRequest);
        } finally {
            // Make sure processes are down even if there was an exception / failure
            if (pA != null) {
                processStopper(pA, true);
            }
            // Archive logs no matter what
            archiveLog(cn, mn, buildLogA);
            if (runLogA != null) {
                // If build failed it is actually expected to have no runtime log.
                archiveLog(cn, mn, runLogA);
            }
            writeReport(cn, mn, whatIDidReport.toString());
            cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
        }
    }

    @Test
    public void manyExtensionsSetA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetA, EnumSet.of(TestFlags.WARM_UP));
        testRuntime(testInfo, supportedExtensionsSubsetSetA, EnumSet.noneOf(TestFlags.class));
    }

    @Test
    public void manyExtensionsSetB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetB, EnumSet.of(TestFlags.WARM_UP));
        testRuntime(testInfo, supportedExtensionsSubsetSetB, EnumSet.noneOf(TestFlags.class));
    }

    @Test
    public void manyReactiveExtensionsSetA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedReactiveExtensionsSubsetSetA, EnumSet.of(TestFlags.WARM_UP, TestFlags.RESTEASY_REACTIVE));
        testRuntime(testInfo, supportedReactiveExtensionsSubsetSetA, EnumSet.of(TestFlags.RESTEASY_REACTIVE));
    }

    @Test
    public void manyReactiveExtensionsSetB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedReactiveExtensionsSubsetSetB, EnumSet.of(TestFlags.WARM_UP, TestFlags.RESTEASY_REACTIVE));
        testRuntime(testInfo, supportedReactiveExtensionsSubsetSetB, EnumSet.of(TestFlags.RESTEASY_REACTIVE));
    }
}
