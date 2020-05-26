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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static io.quarkus.ts.startstop.ArtifactGeneratorTest.supportedExtensionsSubsetSetA;
import static io.quarkus.ts.startstop.ArtifactGeneratorTest.supportedExtensionsSubsetSetB;
import static io.quarkus.ts.startstop.utils.Commands.confAppPropsForSkeleton;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.getGeneratorCommand;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Logs.checkJarSuffixes;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BOM tests for quarkus-maven-plugin generator, command defines BOM via platformArtifactId property
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
@Tag("bomtests")
public class ArtifactGeneratorBOMTest {

    private static final Logger LOGGER = Logger.getLogger(ArtifactGeneratorBOMTest.class.getName());

    public void testRuntime(TestInfo testInfo, String[] extensions, Set<TestFlags> flags) throws Exception {
        ExecutionDetailsForGenerator execution = new ExecutionDetailsForGenerator(testInfo);
        execution.prepareWorkspace();

        FakeOIDCServer fakeOIDCServer = new FakeOIDCServer(6661, "localhost");
        Process runCommandProcess = null;

        try {
            List<String> generatorCmd = getGeneratorCommand(flags, MvnCmds.GENERATOR.mvnCmds[0], extensions, execution.repoDir);
            List<String> buildCmd = getBuildCommand(MvnCmds.JVM.mvnCmds[0], execution.repoDir);
            List<String> runCmd = getRunCommand(MvnCmds.JVM.mvnCmds[1]);
            URLContent skeletonApp = Apps.GENERATED_SKELETON.urlContent;

            //Generator
            execution.reportGeneratorCmd(LOGGER, generatorCmd);
            execution.runCommand(new Commands.ProcessRunner(execution.appBaseDir,execution.generatorLog, generatorCmd, 20));
            checkLog(execution.testClassName, execution.testMethodName, Apps.GENERATED_SKELETON, MvnCmds.GENERATOR, execution.generatorLog);

            // Configure, see app-generated-skeleton/README.md
            confAppPropsForSkeleton(execution.appDir.getAbsolutePath());

            // Build
            execution.reportBuildCmd(LOGGER, buildCmd);
            execution.runCommand(new Commands.ProcessRunner(execution.appDir, execution.buildLog, buildCmd, 20));
            checkLog(execution.testClassName, execution.testMethodName, Apps.GENERATED_SKELETON, MvnCmds.JVM, execution.buildLog);

            // Run
            execution.reportRunCmd(LOGGER, runCmd);
            runCommandProcess = runCommand(runCmd, execution.appDir, execution.runLog);

            // Test web pages
            WebpageTester.testWeb(skeletonApp.urlContent[0][0], 20,
                    skeletonApp.urlContent[0][1], false);

            // Terminate the process
            LOGGER.info("Terminating and scanning logs...");
            runCommandProcess.getInputStream().available();
            checkLog(execution.testClassName, execution.testMethodName, Apps.GENERATED_SKELETON, MvnCmds.JVM, execution.runLog);

            processStopper(runCommandProcess, false);
            assertTrue(waitForTcpClosed("localhost", parsePort(skeletonApp.urlContent[0][0]), 60),
                    "Main port is still open after run");
            checkLog(execution.testClassName, execution.testMethodName, Apps.GENERATED_SKELETON, MvnCmds.JVM, execution.runLog);

            // Check jars
            checkJarSuffixes(flags, execution.appDir);

        } finally {
            fakeOIDCServer.stop();
            processStopper(runCommandProcess, true);
            execution.archiveLogsAndCleanWorkspace();
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
