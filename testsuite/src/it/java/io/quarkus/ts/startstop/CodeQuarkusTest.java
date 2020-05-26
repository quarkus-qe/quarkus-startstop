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
import io.quarkus.ts.startstop.utils.CodeQuarkusExtensions;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.URLContent;
import io.quarkus.ts.startstop.utils.WebpageTester;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.List;

import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for code.quarkus.io generator
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
@Tag("codequarkus")
public class CodeQuarkusTest {

    private static final Logger LOGGER = Logger.getLogger(CodeQuarkusTest.class.getName());

    public static final List<List<CodeQuarkusExtensions>> supportedEx = CodeQuarkusExtensions.partition(4, CodeQuarkusExtensions.Flag.SUPPORTED);
    public static final List<List<CodeQuarkusExtensions>> notSupportedEx = CodeQuarkusExtensions.partition(1, CodeQuarkusExtensions.Flag.NOT_SUPPORTED);
    public static final List<List<CodeQuarkusExtensions>> mixedEx = CodeQuarkusExtensions.partition(1, CodeQuarkusExtensions.Flag.MIXED);

    public void testRuntime(TestInfo testInfo, List<CodeQuarkusExtensions> extensions) throws Exception {
        ExecutionDetailsForGenerator execution = new ExecutionDetailsForGenerator(testInfo, "code-with-quarkus");
        execution.prepareWorkspace();
        Process runCommandProcess = null;

        try {
            List<String> devCmd = getBuildCommand(MvnCmds.MVNW_DEV.mvnCmds[0]);
            URLContent skeletonApp = Apps.GENERATED_SKELETON.urlContent;

            //Generator
            execution.reportDownload(LOGGER, extensions);
            execution.downloadAndUnzip(LOGGER, extensions);

            // Run
            execution.reportRunCmd(LOGGER, devCmd);
            runCommandProcess = runCommand(devCmd, execution.appDir, execution.runLog);

            // Test web pages
            long timeoutS = 10 * 60;
            LOGGER.info("Timeout: " + timeoutS + "s. Waiting for the web content...");
            WebpageTester.testWeb(skeletonApp.urlContent[0][0], timeoutS, skeletonApp.urlContent[0][1], false);

            // Terminate the process
            LOGGER.info("Terminating and scanning logs...");
            runCommandProcess.getInputStream().available();

            processStopper(runCommandProcess, false);
            assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                    "Main port is still open.");
            checkLog(execution.testClassName, execution.testMethodName, Apps.GENERATED_SKELETON, MvnCmds.MVNW_DEV, execution.runLog);

        } finally {
            processStopper(runCommandProcess, true);
            execution.archiveLogsAndCleanWorkspace();
        }
    }

    @Test
    public void supportedExtensionsSubsetA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedEx.get(0));
    }

    @Test
    public void supportedExtensionsSubsetB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedEx.get(1));
    }

    @Test
    public void supportedExtensionsSubsetC(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedEx.get(2));
    }

    @Test
    public void supportedExtensionsSubsetD(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedEx.get(3));
    }

    @Test
    public void notSupportedExtensionsSubset(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, notSupportedEx.get(0).subList(0, Math.min(10, mixedEx.get(0).size())));
    }

    @Test
    public void mixExtensions(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, mixedEx.get(0).subList(0, Math.min(15, mixedEx.get(0).size())));
    }
}
