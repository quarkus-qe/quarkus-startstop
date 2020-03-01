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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.quarkus.ts.startstop.StartStopTest.BASE_DIR;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public class Logs {
    private static final Logger LOGGER = Logger.getLogger(Logs.class.getName());

    private static final Pattern startedPattern = Pattern.compile(".* started in ([0-9\\.]+)s.*", Pattern.DOTALL);
    private static final Pattern stoppedPattern = Pattern.compile(".* stopped in ([0-9\\.]+)s.*", Pattern.DOTALL);
    /*
     Due to console colouring, Windows has control characters in the sequence.
     So "1.778s" in "started in 1.778s." becomes  "[38;5;188m1.778".
     e.g.
     //started in [38;5;188m1.228[39ms.
     //stopped in [38;5;188m0.024[39ms[39m[38;5;203m[39m[38;5;227m

     Although when run from Jenkins service account; those symbols are not present :-)
     This a
     //TODO to make it smoother.
     */
    private static final Pattern startedPatternControlSymbols = Pattern.compile(".* started in .*188m([0-9\\.]+).*", Pattern.DOTALL);
    private static final Pattern stoppedPatternControlSymbols = Pattern.compile(".* stopped in .*188m([0-9\\.]+).*", Pattern.DOTALL);


    // TODO: How about WARNING? Other unwanted messages?
    public static void checkLog(String testClass, String testMethod, Apps app, MvnCmds cmd, File log) throws FileNotFoundException {
        try (Scanner sc = new Scanner(log)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                boolean error = line.matches("(?i:.*ERROR.*)");
                boolean whiteListed = false;
                if (error) {
                    for (String w : app.whitelist.errs) {
                        if (line.contains(w)) {
                            whiteListed = true;
                            LOGGER.info(cmd.name() + "log for " + testMethod + " contains whitelisted error: `" + line + "'");
                            break;
                        }
                    }
                }
                assertFalse(error && !whiteListed, cmd.name() + "log should not contain `ERROR' lines that are not whitelisted. " +
                        "See testsuite" + File.separator + "target" + File.separator + "archived-logs" + File.separator + testClass + File.separator + testMethod + File.separator + log.getName());
            }
        }
    }

    public static void archiveLog(String testClass, String testMethod, File log) throws IOException {
        if (log == null || !log.exists()) {
            LOGGER.severe("log must be a valid, existing file. Skipping operation.");
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
        Files.copy(log.toPath(), Paths.get(destDir.toString(), filename));
    }

    public static Path getLogsDir(String testClass, String testMethod) throws IOException {
        Path destDir = new File(getLogsDir(testClass).toString() + File.separator + testMethod).toPath();
        Files.createDirectories(destDir);
        return destDir;
    }

    public static Path getLogsDir(String testClass) throws IOException {
        Path destDir = new File(BASE_DIR + File.separator + "testsuite" + File.separator + "target" + File.separator + "archived-logs" + File.separator + testClass).toPath();
        Files.createDirectories(destDir);
        return destDir;
    }

    public static void logMeasurements(
            long buildTimeMs, long timeToFirstOKRequestMs, long startedInMs, long stoppedInMs, long rssKb, long openedFiles, Apps app, MvnCmds mode, Path log) throws IOException {

        if (buildTimeMs <= 0) {
            throw new IllegalArgumentException("buildTimeMs must be a positive long, was: " + buildTimeMs);
        }
        if (timeToFirstOKRequestMs <= 0) {
            throw new IllegalArgumentException("timeToFirstOKRequestMs must be a positive long, was: " + timeToFirstOKRequestMs);
        }
        if (startedInMs <= 0) {
            throw new IllegalArgumentException("startedInMs must be a positive long, was: " + startedInMs);
        }
        if (stoppedInMs <= 0) {
            throw new IllegalArgumentException("stoppedInMs must be a positive long, was: " + stoppedInMs);
        }
        if (rssKb <= 0) {
            throw new IllegalArgumentException("rssKb must be a positive long, was: " + rssKb);
        }
        if (openedFiles <= 0) {
            throw new IllegalArgumentException("openedFiles must be a positive long, was: " + openedFiles);
        }
        if (Files.notExists(log)) {
            Files.write(log, "App,Mode,buildTimeMs,timeToFirstOKRequestMs,startedInMs,stoppedInMs,RSS,FDs\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        }
        Files.write(log,
                String.format("%s,%s,%d,%d,%d,%d,%d,%d\n", app.toString(), mode.toString(), buildTimeMs, timeToFirstOKRequestMs, startedInMs, stoppedInMs, rssKb, openedFiles)
                        .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

    public static float[] parseStartStopTimestamps(File log) throws FileNotFoundException {
        float[] startedStopped = new float[]{-1f, -1f};
        try (Scanner sc = new Scanner(log)) {
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
            LOGGER.severe("Parsing start time from log failed. " +
                    "Might not be the good time to call this method. The process might be killed before it wrote to log." +
                    "Find " + log.getName() + " in your target dir.");
        }
        if (startedStopped[1] == -1f) {
            LOGGER.severe("Parsing stop time from log failed. " +
                    "Might not be the good time to call this method. The process might be killed before it wrote to log." +
                    "Find " + log.getName() + " in your target dir.");
        }
        return startedStopped;
    }
}
