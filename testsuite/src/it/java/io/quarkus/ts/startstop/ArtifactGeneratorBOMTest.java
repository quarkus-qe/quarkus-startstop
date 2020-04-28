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
import java.nio.file.Paths;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.quarkus.ts.startstop.ArtifactGeneratorTest.supportedExtensionsSubsetSetA;
import static io.quarkus.ts.startstop.ArtifactGeneratorTest.supportedExtensionsSubsetSetB;
import static io.quarkus.ts.startstop.utils.Commands.cleanDirOrFile;
import static io.quarkus.ts.startstop.utils.Commands.confAppPropsForSkeleton;
import static io.quarkus.ts.startstop.utils.Commands.getArtifactGeneBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.getGeneratorCommand;
import static io.quarkus.ts.startstop.utils.Commands.getLocalMavenRepoDir;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkJarSuffixes;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
@Tag("bomtests")
public class ArtifactGeneratorBOMTest {

    private static final Logger LOGGER = Logger.getLogger(ArtifactGeneratorBOMTest.class.getName());

    public void testRuntime(TestInfo testInfo, String[] extensions, Set<TestFlags> flags) throws Exception {
        Process pA = null;
        File generateLog = null;
        File buildLogA = null;
        File runLogA = null;
        StringBuilder whatIDidReport = new StringBuilder();
        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        File appBaseDir = new File(getArtifactGeneBaseDir());
        File appDir = new File(appBaseDir, Apps.GENERATED_SKELETON.dir);
        String logsDir = appBaseDir.getAbsolutePath() + File.separator + Apps.GENERATED_SKELETON.dir + "-logs";
        String repoDir = getLocalMavenRepoDir();

        List<String> generatorCmd = getGeneratorCommand(flags, MvnCmds.GENERATOR.mvnCmds[0], extensions, repoDir);

        List<String> buildCmd = getBuildCommand(MvnCmds.JVM.mvnCmds[0], repoDir);

        List<String> runCmd = getRunCommand(MvnCmds.JVM.mvnCmds[1]);

        URLContent skeletonApp = Apps.GENERATED_SKELETON.urlContent;

        FakeOIDCServer fakeOIDCServer = new FakeOIDCServer(6661, "localhost");

        try {
            // Cleanup
            cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
            Files.createDirectories(Paths.get(logsDir));
            Files.createDirectories(Paths.get(repoDir));

            //Generator
            LOGGER.info(mn + ": Generator command " + String.join(" ", generatorCmd));
            generateLog = new File(logsDir + File.separator + "bom-artifact-generator.log");
            ExecutorService buildService = Executors.newFixedThreadPool(1);
            buildService.submit(new Commands.ProcessRunner(appBaseDir, generateLog, generatorCmd, 20));
            appendln(whatIDidReport, "# " + cn + ", " + mn);
            appendln(whatIDidReport, (new Date()).toString());
            appendln(whatIDidReport, appBaseDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", generatorCmd));
            buildService.shutdown();
            buildService.awaitTermination(30, TimeUnit.MINUTES);

            assertTrue(generateLog.exists());
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, generateLog);

            // Config, see app-generated-skeleton/README.md
            confAppPropsForSkeleton(appDir.getAbsolutePath());

            // Build
            LOGGER.info(mn + ": Build command " + String.join(" ", buildCmd));
            buildLogA = new File(logsDir + File.separator + "bom-artifact-build.log");
            buildService = Executors.newFixedThreadPool(1);
            buildService.submit(new Commands.ProcessRunner(appDir, buildLogA, buildCmd, 20));
            appendln(whatIDidReport, appDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", buildCmd));
            buildService.shutdown();
            buildService.awaitTermination(30, TimeUnit.MINUTES);

            assertTrue(buildLogA.exists());
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.JVM, buildLogA);

            // Run
            LOGGER.info(mn + ": Run command " + String.join(" ", MvnCmds.JVM.mvnCmds[1]));
            LOGGER.info("Running...");
            runLogA = new File(logsDir + File.separator + "bom-artifact-run.log");
            pA = runCommand(runCmd, appDir, runLogA);
            appendln(whatIDidReport, appDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", runCmd));

            // Test web pages
            WebpageTester.testWeb(skeletonApp.urlContent[0][0], 20,
                    skeletonApp.urlContent[0][1], false);

            LOGGER.info("Terminating test and scanning logs...");
            pA.getInputStream().available();
            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.JVM, runLogA);
            processStopper(pA, false);
            LOGGER.info("Gonna wait for ports closed after run...");
            // Release ports
            assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                    "Main port is still open after run");

            checkLog(cn, mn, Apps.GENERATED_SKELETON, MvnCmds.JVM, runLogA);

            checkJarSuffixes(flags, appDir);
        } finally {
            fakeOIDCServer.stop();

            // Make sure processes are down even if there was an exception / failure
            if (pA != null) {
                processStopper(pA, true);
            }
            // Archive logs no matter what
            archiveLog(cn, mn, generateLog);
            if (buildLogA != null) {
                archiveLog(cn, mn, buildLogA);
            }
            if (runLogA != null) {
                archiveLog(cn, mn, runLogA);
            }
            writeReport(cn, mn, whatIDidReport.toString());
            cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
        }
    }

    @Test
    @Tag("community")
    public void quarkusBomExtensionsA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetA, EnumSet.of(TestFlags.QUARKUS_BOM));
    }

    @Test
    @Tag("community")
    public void quarkusBomExtensionsB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetB, EnumSet.of(TestFlags.QUARKUS_BOM));
    }

    @Test
    @Tag("product")
    public void quarkusProductBomExtensionsA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetA, EnumSet.of(TestFlags.PRODUCT_BOM));
    }

    @Test
    @Tag("product")
    public void quarkusProductBomExtensionsB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetB, EnumSet.of(TestFlags.PRODUCT_BOM));
    }

    @Test
    @Tag("community")
    public void quarkusUniverseBomExtensionsA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetA, EnumSet.of(TestFlags.UNIVERSE_BOM));
    }

    @Test
    @Tag("community")
    public void quarkusUniverseBomExtensionsB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetB, EnumSet.of(TestFlags.UNIVERSE_BOM));
    }

    @Test
    @Tag("product")
    public void quarkusUniverseProductBomExtensionsA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetA, EnumSet.of(TestFlags.UNIVERSE_PRODUCT_BOM));
    }

    @Test
    @Tag("product")
    public void quarkusUniverseProductBomExtensionsB(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedExtensionsSubsetSetB, EnumSet.of(TestFlags.UNIVERSE_PRODUCT_BOM));
    }
}
