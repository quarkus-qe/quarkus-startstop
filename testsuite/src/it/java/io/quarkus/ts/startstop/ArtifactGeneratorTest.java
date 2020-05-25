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
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

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

import static io.quarkus.ts.startstop.utils.Commands.cleanDirOrFile;
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
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.checkThreshold;
import static io.quarkus.ts.startstop.utils.Logs.getLogsDir;
import static io.quarkus.ts.startstop.utils.Logs.parseStartStopTimestamps;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for quarkus-maven-plugin generator
 *
 * @author Michal Karm Babacek <karm@redhat.com>
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

    public static final String[] supportedExtensionsSubsetSetB = new String[]{
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
        ExecutionDetails execution = new ExecutionDetails(testInfo, flags.contains(TestFlags.WARM_UP));
        execution.prepareWorkspace();
        execution.prepareWorkspace();

        FakeOIDCServer fakeOIDCServer = new FakeOIDCServer(6661, "localhost");
        Process runCommandProcess = null;

        try {
            List<String> generatorCmd = getGeneratorCommand(MvnCmds.GENERATOR.mvnCmds[0], extensions);
            List<String> runCmd = getRunCommand(MvnCmds.DEV.mvnCmds[0]);
            URLContent skeletonApp = Apps.GENERATED_SKELETON.urlContent;

            //Generator
            execution.reportGeneratorCmd(LOGGER, generatorCmd);
            long buildTimeMs = execution.runCommand(new Commands.ProcessRunner(execution.appBaseDir,execution.generatorLog, generatorCmd, 20));
            checkLog(execution.testClassName, execution.testMethodName, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, execution.generatorLog);

            // Configure, see app-generated-skeleton/README.md
            confAppPropsForSkeleton(execution.appDir.getAbsolutePath());

            // Run
            execution.reportRunCmd(LOGGER, runCmd);
            runCommandProcess = runCommand(runCmd, execution.appDir, execution.runLog);

            // Test web pages
            long timeoutS = (flags.contains(TestFlags.WARM_UP) ? 20 * 60 : 60); // warm-up needs time to download fresh dependencies
            long timeToFirstOKRequest = WebpageTester.testWeb(skeletonApp.urlContent[0][0], timeoutS,
                    skeletonApp.urlContent[0][1], !flags.contains(TestFlags.WARM_UP));

            // Warm-up finish
            if (flags.contains(TestFlags.WARM_UP)) {
                LOGGER.info("Terminating and scanning logs...");
                runCommandProcess.getInputStream().available();
                processStopper(runCommandProcess, false);
                assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                        "Main port is still open after warm-up");
                checkLog(execution.testClassName, execution.testMethodName, Apps.GENERATED_SKELETON, MvnCmds.DEV, execution.runLog);
                return;
            }

            // Reload
            execution.editFileAndReport(LOGGER);
            long timeToReloadedOKRequest = WebpageTester.testWeb(skeletonApp.urlContent[1][0], 60,
                    skeletonApp.urlContent[1][1], true);

            // RSS and opened files
            long rssKb = getRSSkB(runCommandProcess.pid());
            long openedFiles = getOpenedFDs(runCommandProcess.pid());

            // Terminate the process
            LOGGER.info("Terminating and scanning logs...");
            runCommandProcess.getInputStream().available();

            processStopper(runCommandProcess, false);
            assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                    "Main port is still open after run");
            checkLog(execution.testClassName, execution.testMethodName, Apps.GENERATED_SKELETON, MvnCmds.DEV, execution.runLog);

            // Measurements and threshold check
            float[] startedStopped = parseStartStopTimestamps(execution.runLog);
            LogBuilder.Log log = new LogBuilder()
                    .app(Apps.GENERATED_SKELETON)
                    .mode(MvnCmds.DEV)
                    .buildTimeMs(buildTimeMs)
                    .timeToFirstOKRequestMs(timeToFirstOKRequest)
                    .timeToReloadedOKRequest(timeToReloadedOKRequest)
                    .startedInMs((long) (startedStopped[0] * 1000))
                    .stoppedInMs((long) (startedStopped[1] * 1000))
                    .rssKb(rssKb)
                    .openedFiles(openedFiles)
                    .build();
            execution.measurements(log);
            checkThreshold(Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, SKIP, timeToFirstOKRequest, timeToReloadedOKRequest);
        } finally {
            fakeOIDCServer.stop();
            processStopper(runCommandProcess, true);
            execution.archiveLogsAndCleanWorkspace();
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
}
