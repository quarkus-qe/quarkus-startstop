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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static io.quarkus.ts.startstop.utils.Commands.adjustPrettyPrintForJsonLogging;
import static io.quarkus.ts.startstop.utils.Commands.cleanDirOrFile;
import static io.quarkus.ts.startstop.utils.Commands.download;
import static io.quarkus.ts.startstop.utils.Commands.getArtifactGeneBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.removeRepositoriesAndPluginRepositories;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.unzip;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for code.quarkus.io generator
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
@Tag("codequarkus")
public class CodeQuarkusTest {

    private static final Logger LOGGER = Logger.getLogger(CodeQuarkusTest.class.getName());

    public static final String GEN_BASE_DIR = getArtifactGeneBaseDir();

    public static final List<List<CodeQuarkusExtensions>> supportedEx = CodeQuarkusExtensions.partition(4, CodeQuarkusExtensions.Flag.SUPPORTED);
    public static final List<List<CodeQuarkusExtensions>> notSupportedEx = CodeQuarkusExtensions.partition(1, CodeQuarkusExtensions.Flag.NOT_SUPPORTED);
    public static final List<List<CodeQuarkusExtensions>> mixedEx = CodeQuarkusExtensions.partition(1, CodeQuarkusExtensions.Flag.MIXED);

    public void testRuntime(TestInfo testInfo, List<CodeQuarkusExtensions> extensions) throws Exception {
        Process pA = null;
        File unzipLog = null;
        File runLogA = null;
        StringBuilder whatIDidReport = new StringBuilder();
        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        LOGGER.info(mn + ": Testing Code Quarkus generator with these " + extensions.size() + " extensions: " + extensions.toString());
        File appDir = new File(GEN_BASE_DIR + File.separator + "code-with-quarkus");
        String logsDir = GEN_BASE_DIR + File.separator + "code-with-quarkus-logs";
        List<String> devCmd = getBuildCommand(MvnCmds.MVNW_DEV.mvnCmds[0]);
        URLContent skeletonApp = Apps.GENERATED_SKELETON.urlContent;
        String zipFile = GEN_BASE_DIR + File.separator + "code-with-quarkus.zip";

        try {
            cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
            Files.createDirectories(Paths.get(logsDir));
            appendln(whatIDidReport, "# " + cn + ", " + mn);
            appendln(whatIDidReport, (new Date()).toString());
            LOGGER.info("Downloading...");
            appendln(whatIDidReport, "Download URL: " + download(extensions, zipFile));
            LOGGER.info("Unzipping...");
            unzipLog = unzip(zipFile, GEN_BASE_DIR);
            LOGGER.info("Removing repositories and pluginRepositories from pom.xml ...");
            removeRepositoriesAndPluginRepositories(appDir + File.separator + "pom.xml");
            adjustPrettyPrintForJsonLogging(appDir.getAbsolutePath());
            runLogA = new File(logsDir + File.separator + "dev-run.log");
            LOGGER.info("Running command: " + devCmd + " in directory: " + appDir);
            appendln(whatIDidReport, "Extensions: " + extensions.toString());
            appendln(whatIDidReport, appDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", devCmd));
            pA = runCommand(devCmd, appDir, runLogA);
            // It takes time to download the Internet
            long timeoutS = 10 * 60;
            LOGGER.info("Timeout: " + timeoutS + "s. Waiting for the web content...");
            WebpageTester.testWeb(skeletonApp.urlContent[0][0], timeoutS, skeletonApp.urlContent[0][1], false);
            LOGGER.info("Terminating and scanning logs...");
            pA.getInputStream().available();
            processStopper(pA, false);
            LOGGER.info("Gonna wait for ports closed...");
            assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                    "Main port is still open.");
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.MVNW_DEV, runLogA);
        } finally {
            if (pA != null) {
                processStopper(pA, true);
            }
            archiveLog(cn, mn, unzipLog);
            archiveLog(cn, mn, runLogA);
            writeReport(cn, mn, whatIDidReport.toString());
            cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
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
    public void notsupportedExtensionsSubset(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, notSupportedEx.get(0).subList(0, Math.min(10, mixedEx.get(0).size())));
    }

    @Test
    public void mixExtensions(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, mixedEx.get(0).subList(0, Math.min(15, mixedEx.get(0).size())));
    }
}
