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
package io.quarkus.ts.startstop.utils;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.quarkus.ts.startstop.StartStopTest.BASE_DIR;
import static io.quarkus.ts.startstop.utils.Commands.isThisWindows;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public class Logs {
    private static final Logger LOGGER = Logger.getLogger(Logs.class.getName());

    public static final String jarSuffix = "redhat";
    private static final Pattern jarNamePattern = Pattern.compile("^((?!" + jarSuffix + ").)*jar$");

    private static final Pattern startedPattern = Pattern.compile(".* started in ([0-9\\.]+)s.*", Pattern.DOTALL);
    private static final Pattern stoppedPattern = Pattern.compile(".* stopped in ([0-9\\.]+)s.*", Pattern.DOTALL);
    /*
     Due to console colouring, Windows has control characters in the sequence.
     So "1.778s" in "started in 1.778s." becomes  "[38;5;188m1.778".
     e.g.
     //started in [38;5;188m1.228[39ms.
     //stopped in [38;5;188m0.024[39ms[39m[38;5;203m[39m[38;5;227m

     Although when run from Jenkins service account; those symbols might not be present
     depending on whether you checked AllowInteractingWithDesktop.
     // TODO to make it smoother?
     */
    private static final Pattern startedPatternControlSymbols = Pattern.compile(".* started in .*188m([0-9\\.]+).*", Pattern.DOTALL);
    private static final Pattern stoppedPatternControlSymbols = Pattern.compile(".* stopped in .*188m([0-9\\.]+).*", Pattern.DOTALL);

    private static final Pattern warnErrorDetectionPattern = Pattern.compile("(?i:.*(ERROR|WARN|SLF4J:).*)");
    private static final Pattern devModeError = Pattern.compile(".*Failed to run: Dev mode process did not complete successfully.*");
    private static final Pattern listeningOnDetectionPattern = Pattern.compile("(?i:.*Listening on:.*)");
    private static final Pattern devExpectedHostPattern = Pattern.compile("(?i:.*localhost:.*)");
    private static final Pattern defaultExpectedHostPattern = Pattern.compile("(?i:.*0.0.0.0:.*)");

    public static final long SKIP = -1L;

    public static void checkLog(String testClass, String testMethod, Apps app, MvnCmds cmd, File log) throws IOException {
        try (Scanner sc = new Scanner(log, UTF_8)) {
            Set<String> offendingLines = new HashSet<>();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                boolean error = warnErrorDetectionPattern.matcher(line).matches();
                if (error) {
                    if (isWhiteListed(app.whitelistLogLines.errs, line)) {
                        LOGGER.info(cmd.name() + " log for " + testMethod + " contains whitelisted error: `" + line + "'");
                    } else  if (isWhiteListed(app.whitelistLogLines.platformErrs(), line)) {
                        LOGGER.info(cmd.name() + " log for " + testMethod + " contains platform specific whitelisted error: `" + line + "'");
                    } else {
                        offendingLines.add(line);
                    }
                }
            }

            // Randomly fails when vertx-cache temporary directory exists. Related to https://github.com/quarkusio/quarkus/issues/7678
            // And https://github.com/quarkusio/quarkus/pull/15541/files#diff-a38e0d86cf6a637c19b6e0a0e23959f644886bdcc0f0e5615ce7cfa0e6bc9909R244
            if (Commands.isThisWindows && isDevModeError(offendingLines)) {
            	Stream.of(WhitelistLogLines.WINDOWS_DEV_MODE_ERRORS.errs).forEach(lineToIgnore -> offendingLines.removeIf(line -> lineToIgnore.matcher(line).matches()));
            }

            assertTrue(offendingLines.isEmpty(),
                    cmd.name() + " log should not contain error or warning lines that are not whitelisted. " +
                            "See testsuite" + File.separator + "target" + File.separator + "archived-logs" +
                            File.separator + testClass + File.separator + testMethod + File.separator + log.getName() +
                            " and check these offending lines: \n" + String.join("\n", offendingLines));
        }
    }
    
    public static void checkListeningHost(String testClass, String testMethod, MvnCmds cmd, File log) throws IOException {
    	boolean isOffending = true;
    	try (Scanner sc = new Scanner(log, UTF_8)) {
    		while (sc.hasNextLine()) {
    			String line = sc.nextLine();
    			if (listeningOnDetectionPattern.matcher(line).matches()) { 
    				Pattern expectedHostPattern = defaultExpectedHostPattern;
    				if (cmd == MvnCmds.DEV || cmd == MvnCmds.MVNW_DEV) {
    					expectedHostPattern = devExpectedHostPattern;
    				}
    				
    				isOffending = !expectedHostPattern.matcher(line).matches();
    			}
    		}
    	}
    	
    	assertFalse(isOffending,
                cmd.name() + " log should contain expected listening host. " +
                        "See testsuite" + File.separator + "target" + File.separator + "archived-logs" +
                        File.separator + testClass + File.separator + testMethod + File.separator + log.getName() +
                        " and check the listening host.");
    }

    private static boolean isDevModeError(Set<String> offendingLines) {
        return offendingLines.stream().anyMatch(line -> devModeError.matcher(line).matches());
    }

    private static boolean isWhiteListed(Pattern[] patterns, String line) {
        for (Pattern p : patterns) {
            if (p.matcher(line).matches()) {
                return true;
            }
        }
        return false;
    }

    public static void checkJarSuffixes(Set<TestFlags> flags, File appDir) throws IOException {
        if (flags.contains(TestFlags.PRODUCT_BOM) || flags.contains(TestFlags.UNIVERSE_PRODUCT_BOM)) {
            List<Path> possiblyUnwantedArtifacts = Logs.listJarsFailingNameCheck(
                    appDir.getAbsolutePath() + File.separator + "target" + File.separator + "lib");
            List<String> reportArtifacts = new ArrayList<>();
            boolean containsNotWhitelisted = false;
            for (Path p : possiblyUnwantedArtifacts) {
                boolean found = false;
                for (String w : WhitelistProductBomJars.PRODUCT_BOM.jarNames) {
                    if (p.toString().contains(w)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    reportArtifacts.add("WHITELISTED: " + p);
                } else {
                    containsNotWhitelisted = true;
                    reportArtifacts.add(p.toString());
                }
            }
            assertFalse(containsNotWhitelisted, "There are not-whitelisted artifacts without expected string " + jarSuffix + " suffix, see: \n"
                    + String.join("\n", reportArtifacts));
            if (!reportArtifacts.isEmpty()) {
                LOGGER.warn("There are whitelisted artifacts without expected string " + jarSuffix + " suffix, see: \n"
                        + String.join("\n", reportArtifacts));
            }
        }
    }

    public static void checkThreshold(Apps app, MvnCmds cmd, long rssKb, long timeToFirstOKRequest, long timeToReloadedOKRequest) {
        String propPrefix = isThisWindows ? "windows" : "linux";
        if (cmd == MvnCmds.JVM) {
            propPrefix += ".jvm";
        } else if (cmd == MvnCmds.NATIVE) {
            propPrefix += ".native";
        } else if (cmd == MvnCmds.DEV) {
            propPrefix += ".dev";
        } else if (cmd == MvnCmds.GENERATOR) {
            propPrefix += ".generated.dev";
        } else {
            throw new IllegalArgumentException("Unexpected mode. Check MvnCmds.java.");
        }
        if (timeToFirstOKRequest != SKIP) {
            long timeToFirstOKRequestThresholdMs = app.thresholdProperties.get(propPrefix + ".time.to.first.ok.request.threshold.ms");
            assertTrue(timeToFirstOKRequest <= timeToFirstOKRequestThresholdMs,
                    "Application " + app + " in " + cmd + " mode took " + timeToFirstOKRequest
                            + " ms to get the first OK request, which is over " +
                            timeToFirstOKRequestThresholdMs + " ms threshold.");
        }
        if (rssKb != SKIP) {
            long rssThresholdKb = app.thresholdProperties.get(propPrefix + ".RSS.threshold.kB");
            assertTrue(rssKb <= rssThresholdKb,
                    "Application " + app + " in " + cmd + " consumed " + rssKb + " kB, which is over " +
                            rssThresholdKb + " kB threshold.");
        }
        if (timeToReloadedOKRequest != SKIP) {
            long timeToReloadedOKRequestThresholdMs = app.thresholdProperties.get(propPrefix + ".time.to.reload.threshold.ms");
            assertTrue(timeToReloadedOKRequest <= timeToReloadedOKRequestThresholdMs,
                    "Application " + app + " in " + cmd + " mode took " + timeToReloadedOKRequest
                            + " ms to get the first OK request after dev mode reload, which is over " +
                            timeToReloadedOKRequestThresholdMs + " ms threshold.");
        }
    }

    public static void archiveLog(String testClass, String testMethod, File log) throws IOException {
        if (log == null || !log.exists()) {
            LOGGER.warn("log must be a valid, existing file. Skipping operation.");
            return;
        }
        if (StringUtils.isBlank(testClass)) {
            throw new IllegalArgumentException("testClass must not be blank");
        }
        if (StringUtils.isBlank(testMethod)) {
            throw new IllegalArgumentException("testMethod must not be blank");
        }
        Path destDir = getLogsDir(testClass, testMethod);
        Files.createDirectories(destDir);
        String filename = log.getName();
        Files.copy(log.toPath(), Paths.get(destDir.toString(), filename), REPLACE_EXISTING);
    }

    public static void writeReport(String testClass, String testMethod, String text) throws IOException {
        Path destDir = getLogsDir(testClass, testMethod);
        Files.createDirectories(destDir);
        Files.write(Paths.get(destDir.toString(), "report.md"), text.getBytes(UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Path agregateReport = Paths.get(getLogsDir().toString(), "aggregated-report.md");
        if (Files.notExists(agregateReport)) {
            Files.write(agregateReport, ("# Aggregated Report\n\n").getBytes(UTF_8), StandardOpenOption.CREATE);
        }
        Files.write(agregateReport, text.getBytes(UTF_8), StandardOpenOption.APPEND);
    }

    /**
     * Markdown needs two newlines to make a new paragraph.
     */
    public static void appendln(StringBuilder s, String text) {
        s.append(text);
        s.append("\n\n");
    }

    public static void appendlnSection(StringBuilder s, String text) {
        s.append(text);
        s.append("\n\n---\n");
    }

    public static Path getLogsDir(String testClass, String testMethod) throws IOException {
        Path destDir = new File(getLogsDir(testClass).toString() + File.separator + testMethod).toPath();
        Files.createDirectories(destDir);
        return destDir;
    }

    public static Path getLogsDir(String testClass) throws IOException {
        Path destDir = new File(getLogsDir().toString() + File.separator + testClass).toPath();
        Files.createDirectories(destDir);
        return destDir;
    }

    public static Path getLogsDir() throws IOException {
        Path destDir = new File(BASE_DIR + File.separator + "testsuite" + File.separator + "target" +
                File.separator + "archived-logs").toPath();
        Files.createDirectories(destDir);
        return destDir;
    }

    public static void logMeasurements(LogBuilder.Log log, Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.write(path, (log.headerCSV + "\n").getBytes(UTF_8), StandardOpenOption.CREATE);
        }
        Files.write(path, (log.lineCSV + "\n").getBytes(UTF_8), StandardOpenOption.APPEND);
        LOGGER.info("\n" + log.headerCSV + "\n" + log.lineCSV);
    }

    /**
     * List Jar file names failing regexp pattern check
     *
     * Note the pattern is hardcoded to look for jars not containing word 'redhat',
     * but it could be easily generalized if needed.
     *
     * @param path to the root of directory tree
     * @return list of offending jar paths
     */
    public static List<Path> listJarsFailingNameCheck(String path) throws IOException {
        return Files.find(Paths.get(path),
                500, //if this is not enough, something is broken anyway
                (filePath, fileAttr) -> fileAttr.isRegularFile() && jarNamePattern.matcher(filePath.getFileName().toString()).matches())
                .collect(Collectors.toList());
    }

    public static float[] parseStartStopTimestamps(File log) throws IOException {
        float[] startedStopped = new float[]{-1f, -1f};
        try (Scanner sc = new Scanner(log, UTF_8)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                Matcher m = startedPatternControlSymbols.matcher(line);
                if (startedStopped[0] == -1f && m.matches()) {
                    startedStopped[0] = Float.parseFloat(m.group(1));
                    continue;
                }

                m = startedPattern.matcher(line);
                if (startedStopped[0] == -1f && m.matches()) {
                    startedStopped[0] = Float.parseFloat(m.group(1));
                    continue;
                }

                m = stoppedPatternControlSymbols.matcher(line);
                if (startedStopped[1] == -1f && m.matches()) {
                    startedStopped[1] = Float.parseFloat(m.group(1));
                    continue;
                }

                m = stoppedPattern.matcher(line);
                if (startedStopped[1] == -1f && m.matches()) {
                    startedStopped[1] = Float.parseFloat(m.group(1));
                }
            }
        }
        if (startedStopped[0] == -1f) {
            LOGGER.error("Parsing start time from log failed. " +
                    "Might not be the right time to call this method. The process might have ben killed before it wrote to log." +
                    "Find " + log.getName() + " in your target dir.");
        }
        if (startedStopped[1] == -1f) {
            LOGGER.error("Parsing stop time from log failed. " +
                    "Might not be the right time to call this method. The process might have been killed before it wrote to log." +
                    "Find " + log.getName() + " in your target dir.");
        }
        return startedStopped;
    }
}
