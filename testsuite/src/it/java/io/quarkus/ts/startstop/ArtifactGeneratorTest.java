/*
 * Copyright (c) 2020 Contributors to the Quarkus StartStop project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.Commands;
import io.quarkus.ts.startstop.utils.FakeOIDCServer;
import io.quarkus.ts.startstop.utils.LogBuilder;
import io.quarkus.ts.startstop.utils.Logs;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.TestFlags;
import io.quarkus.ts.startstop.utils.URLContent;
import io.quarkus.ts.startstop.utils.WebpageTester;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.quarkus.ts.startstop.utils.Commands.cleanDir;
import static io.quarkus.ts.startstop.utils.Commands.confAppPropsForSkeleton;
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
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.checkThreshold;
import static io.quarkus.ts.startstop.utils.Logs.getLogsDir;
import static io.quarkus.ts.startstop.utils.Logs.parseStartStopTimestamps;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
@Tag("generator")
public class ArtifactGeneratorTest {

    private static final Logger LOGGER = Logger.getLogger(ArtifactGeneratorTest.class.getName());

    public static final String[] supportedExtensionsSetA = new String[]{
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
            "quartz",
            "reactive-pg-client",
            "rest-client",
            "resteasy",
            "resteasy-jackson",
            "resteasy-jaxb",
            "resteasy-jsonb",
            "scheduler",
            "spring-boot-properties",
            "smallrye-reactive-messaging-amqp",
            "spring-data-jpa",
            "spring-di",
            "spring-security",
            "spring-web",
            "undertow",
            "undertow-websockets",
            "vertx",
            "vertx-web",
    };

    public static final String[] supportedExtensionsSetB = new String[]{
            "agroal",
            "config-yaml",
            "core",
            "hibernate-orm",
            "hibernate-orm-panache",
            "hibernate-validator",
            "jackson",
            "jaxb",
            "jdbc-mariadb",
            "jdbc-mssql",
            "smallrye-context-propagation",
            "smallrye-fault-tolerance",
            "smallrye-health",
            "smallrye-jwt",
            "smallrye-metrics",
            "smallrye-openapi",
            "smallrye-opentracing",
            "smallrye-reactive-messaging",
            "smallrye-reactive-messaging-kafka",
            "smallrye-reactive-streams-operators",
            "spring-data-jpa",
            "spring-di",
            "spring-security",
            "spring-web",
    };

    public void testRuntime(TestInfo testInfo, String[] extensions, Set<TestFlags> flags) throws Exception {
        Process pA = null;
        File buildLogA = null;
        File runLogA = null;
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

        FakeOIDCServer fakeOIDCServer = new FakeOIDCServer(6661, "localhost");

        try {
            // Cleanup
            cleanDir(appDir.getAbsolutePath(), logsDir);
            Files.createDirectories(Paths.get(logsDir));

            // Build
            buildLogA = new File(logsDir + File.separator + (flags.contains(TestFlags.WARM_UP) ? "warmup-artifact-build.log" : "artifact-build.log"));
            ExecutorService buildService = Executors.newFixedThreadPool(1);
            buildService.submit(new Commands.ProcessRunner(appBaseDir, buildLogA, generatorCmd, 20));
            long buildStarts = System.currentTimeMillis();
            buildService.shutdown();
            buildService.awaitTermination(30, TimeUnit.MINUTES);
            long buildEnds = System.currentTimeMillis();

            assertTrue(buildLogA.exists());
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, buildLogA);

            // Config, see app-generated-skeleton/README.md
            confAppPropsForSkeleton(appDir.getAbsolutePath());

            // Run
            LOGGER.info("Running...");
            runLogA = new File(logsDir + File.separator + (flags.contains(TestFlags.WARM_UP) ? "warmup-dev-run.log" : "dev-run.log"));
            pA = runCommand(runCmd, appDir, runLogA);

            // Test web pages
            // The reason for a seemingly large timeout of 20 minutes is that dev mode will be downloading the Internet on the first fresh run.
            long timeoutS = (flags.contains(TestFlags.WARM_UP) ? 20 * 60 : 60);
            long timeToFirstOKRequest = WebpageTester.testWeb(skeletonApp.urlContent[0][0], timeoutS,
                    skeletonApp.urlContent[0][1], true);

            if (flags.contains(TestFlags.WARM_UP)) {
                LOGGER.info("Terminating warmup and scanning logs...");
                pA.getInputStream().available();
                checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, runLogA);
                processStopper(pA, false);
                LOGGER.info("Gonna wait for ports closed after warmup...");
                // Release ports
                assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                        "Main port is still open after warmup");
                checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, runLogA);
                return;
            }

            LOGGER.info("Testing reload...");

            Path srcFile = Paths.get(appDir + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator +
                    "org" + File.separator + "my" + File.separator + "group" + File.separator + "MyResource.java");
            try (Stream<String> src = Files.lines(srcFile)) {
                Files.write(srcFile, src.map(l -> l.replaceAll("hello", "bye")).collect(Collectors.toList()));
            }

            long timeToReloadedOKRequest = WebpageTester.testWeb(skeletonApp.urlContent[1][0], 60,
                    skeletonApp.urlContent[1][1], true);

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
            checkThreshold(Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, SKIP, timeToFirstOKRequest, timeToReloadedOKRequest);

        } finally {
            fakeOIDCServer.stop();

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
            cleanDir(appDir.getAbsolutePath(), logsDir);
        }
    }

    @Test
    public void manyExtensionsSetA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSetA, EnumSet.of(TestFlags.WARM_UP));
        testRuntime(testInfo, supportedExtensionsSetA, EnumSet.noneOf(TestFlags.class));
    }

    @Test
    public void manyExtensionsSetB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSetB, EnumSet.of(TestFlags.WARM_UP));
        testRuntime(testInfo, supportedExtensionsSetB, EnumSet.noneOf(TestFlags.class));
    }
}
