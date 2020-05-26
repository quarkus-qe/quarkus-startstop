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
import io.quarkus.ts.startstop.utils.LogBuilder;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.WebpageTester;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.util.List;

import static io.quarkus.ts.startstop.utils.Commands.getBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.getOpenedFDs;
import static io.quarkus.ts.startstop.utils.Commands.getRSSkB;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Logs.SKIP;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.checkThreshold;
import static io.quarkus.ts.startstop.utils.Logs.parseStartStopTimestamps;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for build and start of applications with some real source code
 * Detains in https://github.com/quarkus-qe/quarkus-startstop#startstoptest
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
@Tag("startstop")
public class StartStopTest {

    private static final Logger LOGGER = Logger.getLogger(StartStopTest.class.getName());

    public static final String BASE_DIR = getBaseDir();

    public void testRuntime(TestInfo testInfo, Apps app, MvnCmds mvnCmds) throws IOException, InterruptedException {
        ExecutionDetailsForStartStop execution = new ExecutionDetailsForStartStop(testInfo, app, mvnCmds);
        execution.prepareWorkspace();
        Process runCommandProcess = null;

        try {
            List<String> buildCmd = getBuildCommand(mvnCmds.mvnCmds[0]);
            List<String> runCmd = getRunCommand(mvnCmds.mvnCmds[1]);

            // Build
            execution.reportBuildCmd(LOGGER, buildCmd);
            long buildTimeMs = execution.runCommand(new Commands.ProcessRunner(execution.appDir,execution.buildLog, buildCmd, 20));
            checkLog(execution.testClassName, execution.testMethodName, app, mvnCmds, execution.buildLog);

            // Run
            execution.reportRunCmd(LOGGER, runCmd);
            runCommandProcess = runCommand(runCmd, execution.appDir, execution.runLog);

            // Test web pages
            LOGGER.info("Waiting for the web content...");
            long timeToFirstOKRequest = WebpageTester.testWeb(app.urlContent.urlContent[0][0], 10,app.urlContent.urlContent[0][1], true);
            for (String[] urlContent : app.urlContent.urlContent) {
                WebpageTester.testWeb(urlContent[0], 5, urlContent[1], false);
            }

            // RSS and opened files
            long rssKb = getRSSkB(runCommandProcess.pid());
            long openedFiles = getOpenedFDs(runCommandProcess.pid());

            // Terminate the process
            LOGGER.info("Terminating and scanning logs...");
            runCommandProcess.getInputStream().available();

            processStopper(runCommandProcess, false);
            assertTrue(waitForTcpClosed("localhost", parsePort(app.urlContent.urlContent[0][0]), 60),
                    "Main port is still open after run");
            checkLog(execution.testClassName, execution.testMethodName, app, mvnCmds, execution.runLog);

            // Measurements and threshold check
            float[] startedStopped = parseStartStopTimestamps(execution.runLog);
            LogBuilder.Log log = new LogBuilder()
                    .app(app)
                    .mode(mvnCmds)
                    .buildTimeMs(buildTimeMs)
                    .timeToFirstOKRequestMs(timeToFirstOKRequest)
                    .startedInMs((long) (startedStopped[0] * 1000))
                    .stoppedInMs((long) (startedStopped[1] * 1000))
                    .rssKb(rssKb)
                    .openedFiles(openedFiles)
                    .build();
            execution.measurements(log);
            checkThreshold(app, mvnCmds, rssKb, timeToFirstOKRequest, SKIP);

        } finally {
            processStopper(runCommandProcess, true);
            execution.archiveLogsAndCleanWorkspace();
        }
    }

    @Test
    public void jaxRsMinimalJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.JVM);
    }

    @Test
    @Tag("native")
    public void jaxRsMinimalNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.JAX_RS_MINIMAL, MvnCmds.NATIVE);
    }

    @Test
    public void fullMicroProfileJVM(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.FULL_MICROPROFILE, MvnCmds.JVM);
    }

    @Test
    @Tag("native")
    public void fullMicroProfileNative(TestInfo testInfo) throws IOException, InterruptedException {
        testRuntime(testInfo, Apps.FULL_MICROPROFILE, MvnCmds.NATIVE);
    }
}