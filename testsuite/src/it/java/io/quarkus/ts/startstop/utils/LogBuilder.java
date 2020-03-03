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

import java.util.Objects;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public class LogBuilder {

    public static class Log {
        public final String header;
        public final String line;

        public Log(String header, String line) {
            this.header = header;
            this.line = line;
        }
    }

    private final String buildTimeMsHeader = "buildTimeMs";
    private long buildTimeMs = -1L;
    private final String timeToFirstOKRequestMsHeader = "timeToFirstOKRequestMs";
    private long timeToFirstOKRequestMs = -1L;
    private final String timeToReloadedOKRequestHeader = "timeToReloadMs";
    private long timeToReloadedOKRequest = -1L;
    private final String startedInMsHeader = "startedInMs";
    private long startedInMs = -1L;
    private final String stoppedInMsHeader = "stoppedInMs";
    private long stoppedInMs = -1L;
    private final String rssKbHeader = "RSSKb";
    private long rssKb = -1L;
    private final String openedFilesHeader = "FDs";
    private long openedFiles = -1L;
    private final String appHeader = "App";
    private Apps app = null;
    private final String modeHeader = "Mode";
    private MvnCmds mode = null;

    public LogBuilder buildTimeMs(long buildTimeMs) {
        if (buildTimeMs <= 0) {
            throw new IllegalArgumentException("buildTimeMs must be a positive long, was: " + buildTimeMs);
        }
        this.buildTimeMs = buildTimeMs;
        return this;
    }

    public LogBuilder timeToFirstOKRequestMs(long timeToFirstOKRequestMs) {
        if (timeToFirstOKRequestMs <= 0) {
            throw new IllegalArgumentException("timeToFirstOKRequestMs must be a positive long, was: " + timeToFirstOKRequestMs);
        }
        this.timeToFirstOKRequestMs = timeToFirstOKRequestMs;
        return this;
    }

    public LogBuilder timeToReloadedOKRequest(long timeToReloadedOKRequest) {
        if (timeToReloadedOKRequest <= 0) {
            throw new IllegalArgumentException("timeToReloadedOKRequest must be a positive long, was: " + timeToFirstOKRequestMs);
        }
        this.timeToReloadedOKRequest = timeToReloadedOKRequest;
        return this;
    }

    public LogBuilder startedInMs(long startedInMs) {
        if (startedInMs <= 0) {
            throw new IllegalArgumentException("startedInMs must be a positive long, was: " + startedInMs);
        }
        this.startedInMs = startedInMs;
        return this;
    }

    public LogBuilder stoppedInMs(long stoppedInMs) {
        if (stoppedInMs <= 0) {
            throw new IllegalArgumentException("stoppedInMs must be a positive long, was: " + stoppedInMs);
        }
        this.stoppedInMs = stoppedInMs;
        return this;
    }

    public LogBuilder rssKb(long rssKb) {
        if (rssKb <= 0) {
            throw new IllegalArgumentException("rssKb must be a positive long, was: " + rssKb);
        }
        this.rssKb = rssKb;
        return this;
    }

    public LogBuilder openedFiles(long openedFiles) {
        if (openedFiles <= 0) {
            throw new IllegalArgumentException("openedFiles must be a positive long, was: " + openedFiles);
        }
        this.openedFiles = openedFiles;
        return this;
    }

    public LogBuilder app(Apps app) {
        Objects.requireNonNull(app, "Valid app flavour must be provided");
        this.app = app;
        return this;
    }

    public LogBuilder mode(MvnCmds mode) {
        Objects.requireNonNull(mode, "Valid app flavour must be provided");
        this.mode = mode;
        return this;
    }

    public Log build() {
        StringBuilder h = new StringBuilder(512);
        StringBuilder l = new StringBuilder(512);
        if (app != null) {
            h.append(appHeader);
            h.append(',');
            l.append(app);
            l.append(',');
        }
        if (mode != null) {
            h.append(modeHeader);
            h.append(',');
            l.append(mode);
            l.append(',');
        }
        if (buildTimeMs != -1L) {
            h.append(buildTimeMsHeader);
            h.append(',');
            l.append(buildTimeMs);
            l.append(',');
        }
        if (timeToFirstOKRequestMs != -1L) {
            h.append(timeToFirstOKRequestMsHeader);
            h.append(',');
            l.append(timeToFirstOKRequestMs);
            l.append(',');
        }
        if (timeToReloadedOKRequest != -1L) {
            h.append(timeToReloadedOKRequestHeader);
            h.append(',');
            l.append(timeToReloadedOKRequest);
            l.append(',');
        }
        if (startedInMs != -1L) {
            h.append(startedInMsHeader);
            h.append(',');
            l.append(startedInMs);
            l.append(',');
        }
        if (stoppedInMs != -1L) {
            h.append(stoppedInMsHeader);
            h.append(',');
            l.append(stoppedInMs);
            l.append(',');
        }
        if (rssKb != -1L) {
            h.append(rssKbHeader);
            h.append(',');
            l.append(rssKb);
            l.append(',');
        }
        if (openedFiles != -1L) {
            h.append(openedFilesHeader);
            h.append(',');
            l.append(openedFiles);
            l.append(',');
        }
        String header = h.toString();
        String line = l.toString();
        // Strip trailing ','
        return new Log(header.substring(0, header.length() - 1), line.substring(0, line.length() - 1));
    }
}
