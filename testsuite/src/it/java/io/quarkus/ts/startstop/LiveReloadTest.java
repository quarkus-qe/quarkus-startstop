package io.quarkus.ts.startstop;

import static io.quarkus.ts.startstop.utils.Commands.cleanTarget;
import static io.quarkus.ts.startstop.utils.Commands.getBaseDir;
import static io.quarkus.ts.startstop.utils.Commands.getRunCommand;
import static io.quarkus.ts.startstop.utils.Commands.processStopper;
import static io.quarkus.ts.startstop.utils.Commands.runCommand;
import static io.quarkus.ts.startstop.utils.Logs.appendln;
import static io.quarkus.ts.startstop.utils.Logs.appendlnSection;
import static io.quarkus.ts.startstop.utils.Logs.archiveLog;
import static io.quarkus.ts.startstop.utils.Logs.writeReport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.quarkus.ts.startstop.utils.Apps;
import io.quarkus.ts.startstop.utils.HttpUtils;
import io.quarkus.ts.startstop.utils.MvnCmds;

@Tag("instrumentation")
public class LiveReloadTest {
    private static final String BASE_DIR = getBaseDir();
    private static final String BASE_URL = "http://localhost:8080";
    private static final Apps APP = Apps.LIVE_RELOAD;
    private static final File APP_DIR = new File(BASE_DIR + File.separator + APP.dir);
    private static final MvnCmds MVN_CMDS = MvnCmds.DEV;
    private static final List<String> RUN_COMMAND = getRunCommand(MVN_CMDS.mvnCmds[0]);
    private static final Path ADDED_FILE_PATH = Paths.get(APP_DIR.toString(),
            "src/main/java/com/example/quarkus/AnotherClass.java");
    private static final byte[] ADDED_FILE_CONTENT = "package com.example.quarkus;\nclass ItDoesntMatter{}"
            .getBytes(StandardCharsets.UTF_8);
    private static final File HELLO_RESOURCE_FILE = new File(APP_DIR, "src/main/java/com/example/quarkus/HelloResource.java");
    private static final String HELLO_RETURN_TEMPLATE = "return \"%s\"";
    private static final String HELLO_STANDARD_RESPONSE = "hello";
    private static final String HELLO_MODIFIED_RESPONSE = "cheers";

    private Process process;
    private File processLog;
    private StringBuilder whatIDidReport;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) throws Exception {
        // cleanup
        cleanTarget(APP);
        Files.createDirectories(Paths.get(APP_DIR.getAbsolutePath() + File.separator + "logs"));
        // logs
        processLog = new File(APP_DIR.getAbsolutePath() + File.separator + "logs" + File.separator
                + MVN_CMDS.name().toLowerCase() + "-run.log");
        whatIDidReport = new StringBuilder();
        appendln(whatIDidReport, "# " + getTestClassName(testInfo) + ", " + getTestMethodName(testInfo));
        appendln(whatIDidReport, (new Date()).toString());
        appendln(whatIDidReport, APP_DIR.getAbsolutePath());
        appendlnSection(whatIDidReport, String.join(" ", RUN_COMMAND));
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) throws Exception {
        // kill
        if (process != null) {
            processStopper(process, true);
            process = null;
        }
        // logs
        String cn = getTestClassName(testInfo);
        String mn = getTestMethodName(testInfo);
        archiveLog(cn, mn, processLog);
        writeReport(cn, mn, whatIDidReport.toString());
        // cleanup
        Files.deleteIfExists(ADDED_FILE_PATH);
        cleanTarget(APP);
    }

    @ParameterizedTest
    @EnumSource
    public void testAddClass(InstrumentationSettings instrumentationSettings) throws IOException {
        startApp();
        handleInstrumentationSettings(instrumentationSettings);
        callHello();
        String firstUuid = getUuid();
        addClass();
        callHello();
        String secondUuid = getUuid();
        assertInstrumentation(instrumentationSettings.addClassInstrumentation, firstUuid, secondUuid);
    }

    @ParameterizedTest
    @EnumSource
    public void testDeleteClass(InstrumentationSettings instrumentationSettings) throws IOException {
        addClass();
        startApp();
        handleInstrumentationSettings(instrumentationSettings);
        callHello();
        String firstUuid = getUuid();
        deleteClass();
        callHello();
        String secondUuid = getUuid();
        assertInstrumentation(instrumentationSettings.deleteClassInstrumentation, firstUuid, secondUuid);
    }

    @ParameterizedTest
    @EnumSource
    public void testModifyClass(InstrumentationSettings instrumentationSettings) throws IOException {
        try {
            startApp();
            handleInstrumentationSettings(instrumentationSettings);
            callHello();
            String firstUuid = getUuid();
            modifyHelloReturnValue(HELLO_STANDARD_RESPONSE, HELLO_MODIFIED_RESPONSE);
            callHello();
            String secondUuid = getUuid();
            assertInstrumentation(instrumentationSettings.modifyClassInstrumentation, firstUuid, secondUuid);
        } finally {
            modifyHelloReturnValue(HELLO_MODIFIED_RESPONSE, HELLO_STANDARD_RESPONSE);
        }
    }

    private String getTestClassName(TestInfo testInfo) {
        return testInfo.getTestClass().map(Class::getCanonicalName).orElse("Unknown class");
    }

    private String getTestMethodName(TestInfo testInfo) {
        return testInfo.getTestMethod().map(Method::getName).orElse("Unknown method") + "_"
                + testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }

    private void handleInstrumentationSettings(InstrumentationSettings instrumentationSettings) {
        switch (instrumentationSettings) {
            case INSTRUMENTATION_ENABLED:
                enableInstrumentation();
                break;
            case INSTRUMENTATION_DISABLED:
                disableInstrumentation();
                break;
            case INSTRUMENTATION_DEFAULT:
                break;
        }
    }

    private void assertInstrumentation(boolean doInstrumentation, String firstUuid, String secondUuid) {
        if (doInstrumentation) {
            Assertions.assertEquals(firstUuid, secondUuid);
        } else {
            Assertions.assertNotEquals(firstUuid, secondUuid);
        }
    }

    private void enableInstrumentation() {
        HttpUtils.getHttpResponse(BASE_URL + "/enable");
    }

    private void disableInstrumentation() {
        HttpUtils.getHttpResponse(BASE_URL + "/disable");
    }

    private void startApp() {
        process = runCommand(RUN_COMMAND, APP_DIR, processLog);
    }

    private void callHello() {
        HttpUtils.getHttpResponse(BASE_URL + "/hello");
    }

    private String getUuid() {
        return HttpUtils.getHttpResponse(BASE_URL + "/uuid");
    }

    private void addClass() throws IOException {
        Files.write(ADDED_FILE_PATH, ADDED_FILE_CONTENT);
    }

    private void deleteClass() throws IOException {
        Files.delete(ADDED_FILE_PATH);
    }

    private void modifyHelloReturnValue(String oldContent, String newContent) throws IOException {
        Assertions.assertTrue(HELLO_RESOURCE_FILE.isFile());
        FileUtils.write(HELLO_RESOURCE_FILE,
                FileUtils.readFileToString(HELLO_RESOURCE_FILE, StandardCharsets.UTF_8).replace(
                        String.format(HELLO_RETURN_TEMPLATE, oldContent), String.format(HELLO_RETURN_TEMPLATE, newContent)),
                StandardCharsets.UTF_8);
    }

    private enum InstrumentationSettings {
        INSTRUMENTATION_ENABLED(false, false, true),
        INSTRUMENTATION_DISABLED(false, false, false),
        INSTRUMENTATION_DEFAULT(false, false, false);

        private final boolean addClassInstrumentation;
        private final boolean deleteClassInstrumentation;
        private final boolean modifyClassInstrumentation;

        InstrumentationSettings(boolean addClassInstrumentation, boolean deleteClassInstrumentation,
                boolean modifyClassInstrumentation) {
            this.addClassInstrumentation = addClassInstrumentation;
            this.deleteClassInstrumentation = deleteClassInstrumentation;
            this.modifyClassInstrumentation = modifyClassInstrumentation;
        }
    }
}
