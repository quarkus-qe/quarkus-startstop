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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static io.quarkus.ts.startstop.StartStopTest.BASE_DIR;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public class Commands {
    private static final Logger LOGGER = Logger.getLogger(Commands.class.getName());

    public static final boolean isThisWindows = System.getProperty("os.name").matches(".*[Ww]indows.*");
    private static final Pattern numPattern = Pattern.compile("[ \t]*[0-9]+[ \t]*");

    public static String getBaseDir() {
        String env = System.getenv().get("basedir");
        String sys = System.getProperty("basedir");
        if (StringUtils.isNotBlank(env)) {
            return new File(env).getParent();
        }
        if (StringUtils.isBlank(sys)) {
            throw new IllegalArgumentException("Unable to determine project.basedir.");
        }
        return new File(sys).getParent();
    }

    public static void cleanTarget(Apps app) {
        String target = BASE_DIR + File.separator + app.dir + File.separator + "target";
        String logs = BASE_DIR + File.separator + app.dir + File.separator + "logs";
        try {
            FileUtils.deleteDirectory(new File(target));
            FileUtils.deleteDirectory(new File(logs));
        } catch (IOException e) {
            // Silence is golden
        }
        (new File(target)).deleteOnExit();
        (new File(logs)).deleteOnExit();
    }

    public static boolean waitForTcpClosed(String host, int port, long loopTimeoutS) throws InterruptedException, UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        long now = System.currentTimeMillis();
        long startTime = now;
        InetSocketAddress socketAddr = new InetSocketAddress(address, port);
        while (now - startTime < 1000 * loopTimeoutS) {
            try (Socket socket = new Socket()) {
                // If it let's you write something there, it is still ready.
                socket.connect(socketAddr, 1000);
                socket.setSendBufferSize(1);
                socket.getOutputStream().write(1);
                socket.shutdownInput();
                socket.shutdownOutput();
                LOGGER.info("Socket still available: " + host + ":" + port);
            } catch (IOException e) {
                // Exception thrown - socket is likely closed.
                return true;
            }
            Thread.sleep(1000);
            now = System.currentTimeMillis();
        }
        return false;
    }

    public static int parsePort(String url) {
        return Integer.parseInt(url.split(":")[2].split("/")[0]);
    }

    public static Process runCommand(String[] command, File directory, File logFile) {
        ProcessBuilder pa;
        if (isThisWindows) {
            pa = new ProcessBuilder(ArrayUtils.addAll(new String[]{"cmd", "/C"}, command));
        } else {
            pa = new ProcessBuilder(command);
        }
        Map<String, String> envA = pa.environment();
        envA.put("PATH", System.getenv("PATH"));
        pa.directory(directory);
        pa.redirectErrorStream(true);
        pa.redirectOutput(ProcessBuilder.Redirect.to(logFile));
        Process pA = null;
        try {
            pA = pa.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pA;
    }

    public static void pidKiller(long pid, boolean force) {
        try {
            if (isThisWindows) {
                if (!force) {
                    Process p = Runtime.getRuntime().exec(new String[]{
                            BASE_DIR + File.separator + "testsuite" + File.separator + "src" + File.separator + "it" + File.separator + "resources" + File.separator +
                                    "CtrlC.exe ", Long.toString(pid)});
                    p.waitFor(1, TimeUnit.MINUTES);
                }
                Runtime.getRuntime().exec(new String[]{"cmd", "/C", "taskkill", "/PID", Long.toString(pid), "/F", "/T"});
            } else {
                Runtime.getRuntime().exec(new String[]{"kill", force ? "-9" : "-15", Long.toString(pid)});
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public static long getRSSkB(long pid) throws IOException, InterruptedException {
        ProcessBuilder pa;
        if (isThisWindows) {
            // Note that PeakWorkingSetSize might be better, but we would need to change it on Linux too...
            // https://docs.microsoft.com/en-us/windows/win32/cimwin32prov/win32-process
            pa = new ProcessBuilder("wmic", "process", "where", "processid=" + pid, "get", "WorkingSetSize");
        } else {
            pa = new ProcessBuilder("ps", "--no-headers", "-p", Long.toString(pid), "-o", "rss");
        }
        Map<String, String> envA = pa.environment();
        envA.put("PATH", System.getenv("PATH"));
        pa.redirectErrorStream(true);
        Process p = pa.start();
        try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String l;
            while ((l = processOutputReader.readLine()) != null) {
                if (numPattern.matcher(l).matches()) {
                    if (isThisWindows) {
                        // Qualifiers: DisplayName ("Working Set Size"), Units ("bytes")
                        return Long.parseLong(l.trim()) / 1024L;
                    } else {
                        return Long.parseLong(l.trim());
                    }
                }
            }
            p.waitFor();
        }
        return -1L;
    }

    public static long getOpenedFDs(long pid) throws IOException, InterruptedException {
        ProcessBuilder pa;
        long count = 0;
        if (isThisWindows) {
            pa = new ProcessBuilder("wmic", "process", "where", "processid=" + pid, "get", "HandleCount");
        } else {
            pa = new ProcessBuilder("lsof", "-F0n", "-p", Long.toString(pid));
        }
        Map<String, String> envA = pa.environment();
        envA.put("PATH", System.getenv("PATH"));
        pa.redirectErrorStream(true);
        Process p = pa.start();
        try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            if (isThisWindows) {
                String l;
                // TODO: We just get a magical number with all FDs... Is it O.K.?
                while ((l = processOutputReader.readLine()) != null) {
                    if (numPattern.matcher(l).matches()) {
                        return Long.parseLong(l.trim());
                    }
                }
            } else {
                // TODO: For the time being we count apples and oranges; we might want to distinguish .so and .jar ?
                while (processOutputReader.readLine() != null) {
                    count++;
                }
            }
            p.waitFor();
        }
        return count;
    }

    /*
    TODO: CPU cycles used

    Pros: good data
    Cons: dependency on perf tool; will not translate to Windows data

    karm@local:~/workspaceRH/fooBar$ perf stat java -jar target/fooBar-1.0.0-SNAPSHOT-runner.jar
    2020-02-25 16:07:00,870 INFO  [io.quarkus] (main) fooBar 1.0.0-SNAPSHOT (running on Quarkus 999-SNAPSHOT) started in 0.776s.
    2020-02-25 16:07:00,873 INFO  [io.quarkus] (main) Profile prod activated.
    2020-02-25 16:07:00,873 INFO  [io.quarkus] (main) Installed features: [amazon-lambda, cdi, resteasy]
    2020-02-25 16:07:03,360 INFO  [io.quarkus] (main) fooBar stopped in 0.018s

    Performance counter stats for 'java -jar target/fooBar-1.0.0-SNAPSHOT-runner.jar':

       1688.910052      task-clock:u (msec)       #    0.486 CPUs utilized
                 0      context-switches:u        #    0.000 K/sec
                 0      cpu-migrations:u          #    0.000 K/sec
            12,865      page-faults:u             #    0.008 M/sec
     4,274,799,448      cycles:u                  #    2.531 GHz
     4,325,761,598      instructions:u            #    1.01  insn per cycle
       919,713,769      branches:u                #  544.561 M/sec
        29,310,015      branch-misses:u           #    3.19% of all branches

       3.473028811 seconds time elapsed
    */

    public static void processStopper(Process p, boolean force) throws InterruptedException, IOException {
        p.children().forEach(child -> {
            if (child.supportsNormalTermination()) {
                child.destroy();
            }
            pidKiller(child.pid(), force);
        });
        if (p.supportsNormalTermination()) {
            p.destroy();
            p.waitFor(3, TimeUnit.MINUTES);
        }
        pidKiller(p.pid(), force);
    }

    public static class ProcessRunner implements Runnable {
        final File directory;
        final File log;
        final String[] command;
        final long timeoutMinutes;

        public ProcessRunner(File directory, File log, String[] command, long timeoutMinutes) {
            this.directory = directory;
            this.log = log;
            this.command = command;
            this.timeoutMinutes = timeoutMinutes;
        }

        @Override
        public void run() {
            ProcessBuilder pb;
            if (isThisWindows) {
                pb = new ProcessBuilder(ArrayUtils.addAll(new String[]{"cmd", "/C"}, command));
            } else {
                pb = new ProcessBuilder(ArrayUtils.addAll(command));
            }
            Map<String, String> env = pb.environment();
            env.put("PATH", System.getenv("PATH"));
            pb.directory(directory);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.to(log));
            Process p = null;
            try {
                p = pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Objects.requireNonNull(p).waitFor(timeoutMinutes, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
