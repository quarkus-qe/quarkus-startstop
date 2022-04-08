package io.quarkus.ts.startstop;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.CodeQuarkusExtensions;
import io.quarkus.ts.startstop.utils.Commands;
import io.quarkus.ts.startstop.utils.MvnCmds;
import io.quarkus.ts.startstop.utils.URLContent;
import io.quarkus.ts.startstop.utils.WebpageTester;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.quarkus.ts.startstop.utils.Commands.adjustPrettyPrintForJsonLogging;
import static io.quarkus.ts.startstop.utils.Commands.cleanDirOrFile;
import static io.quarkus.ts.startstop.utils.Commands.disableDevServices;
import static io.quarkus.ts.startstop.utils.Commands.download;
import static io.quarkus.ts.startstop.utils.Commands.dropEntityAnnotations;
import static io.quarkus.ts.startstop.utils.Commands.getArtifactGeneBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getBuildCommand;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.parsePort;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.removeRepositoriesAndPluginRepositories;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Commands.unzip;
import static io.quarkus.ts.startstop.utils.Commands.waitForTcpClosed;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.checkListeningHost;
import static io.quarkus.ts.startstop.utils.Logs.checkLog;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for code.quarkus.io generator
 */
@Tag("codequarkus")
public class CodeQuarkusTest {

    private static final Logger LOGGER = Logger.getLogger(CodeQuarkusTest.class.getName());

    public static final String GEN_BASE_DIR = getArtifactGeneBaseDir();

    public static final List<List<CodeQuarkusExtensions>> supportedEx = CodeQuarkusExtensions.partition(4, CodeQuarkusExtensions.Flag.SUPPORTED);
    public static final List<List<CodeQuarkusExtensions>> notSupportedEx = CodeQuarkusExtensions.partition(1, CodeQuarkusExtensions.Flag.NOT_SUPPORTED);
    public static final List<List<CodeQuarkusExtensions>> mixedEx = CodeQuarkusExtensions.partition(1, CodeQuarkusExtensions.Flag.MIXED);
    
    public static final Stream<List<CodeQuarkusExtensions>> supportedExWithCodeStarter() {
        return Arrays.asList(
                Arrays.asList(CodeQuarkusExtensions.QUARKUS_SMALLRYE_HEALTH, CodeQuarkusExtensions.QUARKUS_RESTEASY),
                Arrays.asList(CodeQuarkusExtensions.QUARKUS_LOGGING_JSON, CodeQuarkusExtensions.QUARKUS_RESTEASY),
                Arrays.asList(CodeQuarkusExtensions.QUARKUS_RESTEASY),
                Arrays.asList(CodeQuarkusExtensions.QUARKUS_SPRING_WEB),
                Arrays.asList(CodeQuarkusExtensions.QUARKUS_WEBSOCKETS)
        ).stream();
    }

    public void testRuntime(TestInfo testInfo, List<CodeQuarkusExtensions> extensions, MvnCmds mvnCmds) throws Exception {
        Process pA = null;
        File unzipLog = null;
        File buildLogA = null;
        File runLogA = null;
        StringBuilder whatIDidReport = new StringBuilder();
        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        LOGGER.info(mn + ": Testing Code Quarkus generator with these " + extensions.size() + " extensions: " + extensions.toString() + ", mode: " + mvnCmds.toString());
        File appDir = new File(GEN_BASE_DIR + File.separator + "code-with-quarkus");
        String logsDir = GEN_BASE_DIR + File.separator + "code-with-quarkus-logs";
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
            disableDevServices(appDir.getAbsolutePath());
            dropEntityAnnotations(appDir.getAbsolutePath());
            List<String> cmd;
            // Build
            if (mvnCmds != MvnCmds.MVNW_DEV) {
                buildLogA = new File(logsDir + File.separator + "build.log");
                ExecutorService buildService = Executors.newFixedThreadPool(1);
                List<String> baseBuildCmd = new ArrayList<>();
                baseBuildCmd.addAll(Arrays.asList(mvnCmds.mvnCmds[0]));
                cmd = getBuildCommand(baseBuildCmd.toArray(new String[0]));

                appendln(whatIDidReport, "# " + cn + ", " + mn);
                appendln(whatIDidReport, (new Date()).toString());
                appendln(whatIDidReport, appDir.getAbsolutePath());
                appendln(whatIDidReport, "Extensions: " + extensions.toString());
                appendlnSection(whatIDidReport, String.join(" ", cmd));

                LOGGER.info("Building (" + cmd + ")");
                buildService.submit(new Commands.ProcessRunner(appDir, buildLogA, cmd, 20));

                buildService.shutdown();
                buildService.awaitTermination(30, TimeUnit.MINUTES);

                assertTrue(buildLogA.exists());
            }
            
            // Run
            runLogA = new File(logsDir + File.separator + "dev-run.log");
            if (mvnCmds == MvnCmds.MVNW_DEV) {
                cmd = getBuildCommand(mvnCmds.mvnCmds[0]);
            } else {
                cmd = getRunCommand(mvnCmds.mvnCmds[1]);
            }
            
            LOGGER.info("Running (" + cmd + ") in directory: " + appDir);
            appendln(whatIDidReport, "Extensions: " + extensions.toString());
            appendln(whatIDidReport, appDir.getAbsolutePath());
            appendlnSection(whatIDidReport, String.join(" ", cmd));
            
            pA = runCommand(cmd, appDir, runLogA);
            
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
            checkLog(cn, mn, Apps.GENERATED_SKELETON, mvnCmds, runLogA);
            checkListeningHost(cn, mn, mvnCmds, runLogA);
        } finally {
            if (pA != null) {
                processStopper(pA, true);
            }
            
            String tag = StringUtils.EMPTY;
            if (extensions.size() == 1) {
            	tag = "-" + extensions.get(0).id;
            }
            
            archiveLog(cn, mn + tag, unzipLog);
            archiveLog(cn, mn + tag, buildLogA);
            archiveLog(cn, mn + tag, runLogA);
            writeReport(cn, mn + tag, whatIDidReport.toString());
            cleanDirOrFile(appDir.getAbsolutePath(), logsDir);
        }
    }

    @Test
    public void supportedExtensionsSubsetA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedEx.get(0), MvnCmds.MVNW_DEV);
    }

    @Test
    public void supportedExtensionsSubsetB(TestInfo testInfo) throws Exception {
        List<CodeQuarkusExtensions> supportedExtensionsSubsetB = supportedEx.get(1);
        // quarkus-reactive-routes extension to open port 8080 and provide index.html file
        // https://github.com/quarkusio/quarkus/issues/21132
        supportedExtensionsSubsetB.add(CodeQuarkusExtensions.QUARKUS_REACTIVE_ROUTES);
        testRuntime(testInfo, supportedExtensionsSubsetB, MvnCmds.MVNW_DEV);
    }

    @Test
    public void supportedExtensionsSubsetC(TestInfo testInfo) throws Exception {
        List<CodeQuarkusExtensions> supportedExtensionsSubsetC = supportedEx.get(2);
        // resteasy or spring-web extension is needed to provide index.html file
        // content from index.html file is checked to ensure the application is up and running
        supportedExtensionsSubsetC.add(CodeQuarkusExtensions.QUARKUS_RESTEASY);
        testRuntime(testInfo, supportedExtensionsSubsetC, MvnCmds.MVNW_DEV);
    }

    @Test
    public void supportedExtensionsSubsetD(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, supportedEx.get(3), MvnCmds.MVNW_DEV);
    }

    @Test
    public void notSupportedExtensionsSubsetA(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, notSupportedEx.get(0).subList(0, Math.min(10, notSupportedEx.get(0).size())), MvnCmds.MVNW_DEV);
    }
    
    @Test
    public void notSupportedExtensionsSubsetB(TestInfo testInfo) throws Exception {
        List<CodeQuarkusExtensions> notSupportedExtensionsSubsetB = notSupportedEx.get(0).subList(Math.min(10, notSupportedEx.get(0).size()), Math.min(20, notSupportedEx.get(0).size()));
        // resteasy or spring-web extension is needed to provide index.html file
        // content from index.html file is checked to ensure the application is up and running
        notSupportedExtensionsSubsetB.add(CodeQuarkusExtensions.QUARKUS_RESTEASY);
        testRuntime(testInfo, notSupportedExtensionsSubsetB, MvnCmds.MVNW_DEV);
    }

    @Test
    public void mixExtensions(TestInfo testInfo) throws Exception {
        testRuntime(testInfo, mixedEx.get(0).subList(0, Math.min(20, mixedEx.get(0).size())), MvnCmds.MVNW_DEV);
    }

    @Test
    public void java17BasedProject(TestInfo testInfo) throws Exception {
        StringBuilder whatIDidReport = new StringBuilder();
        String cn = testInfo.getTestClass().get().getCanonicalName();
        String mn = testInfo.getTestMethod().get().getName();
        File appDir = new File(GEN_BASE_DIR + File.separator + "code-with-quarkus");
        String zipFile = GEN_BASE_DIR + File.separator + "code-with-quarkus.zip";

        try {
            appendln(whatIDidReport, "# " + cn + ", " + mn);
            appendln(whatIDidReport, (new Date()).toString());
            LOGGER.info("Downloading...");
            appendln(whatIDidReport, "Download URL: " + download(List.of(CodeQuarkusExtensions.QUARKUS_RESTEASY), zipFile, 17));
            LOGGER.info("Unzipping...");
            unzip(zipFile, GEN_BASE_DIR);

            String pom = Files.readString(Paths.get(GEN_BASE_DIR + File.separator + "code-with-quarkus" + File.separator + "pom.xml"));
            assertTrue(pom.contains("<maven.compiler.release>17"), "Downloaded app doesn't have pom.xml file Java 17 based");

        } finally {
            writeReport(cn, mn, whatIDidReport.toString());
            cleanDirOrFile(appDir.getAbsolutePath());
        }
    }

    @ParameterizedTest
    @MethodSource("supportedExWithCodeStarter")
    public void supportedExtensionWithCodeStarterWorksInJVM(List<CodeQuarkusExtensions> extensions, TestInfo testInfo) throws Exception {
        testRuntime(testInfo, extensions, MvnCmds.MVNW_JVM);
    }
    
    @Tag("native")
    @ParameterizedTest
    @MethodSource("supportedExWithCodeStarter")
    public void supportedExtensionWithCodeStarterWorksInNative(List<CodeQuarkusExtensions> extensions, TestInfo testInfo) throws Exception {
        testRuntime(testInfo, extensions, MvnCmds.MVNW_NATIVE);
    }
}
