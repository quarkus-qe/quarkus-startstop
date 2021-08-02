package io.quarkus.ts.startstop.utils;

import java.util.Objects;

public class LogBuilder {

    public static class Log {
        public final String headerCSV;
        public final String headerMarkdown;
        public final String lineCSV;
        public final String lineMarkdown;

        public Log(String headerCSV, String headerMarkdown, String lineCSV, String lineMarkdown) {
            this.headerCSV = headerCSV;
            this.headerMarkdown = headerMarkdown;
            this.lineCSV = lineCSV;
            this.lineMarkdown = lineMarkdown;
        }
    }

    private static final String buildTimeMsHeader = "buildTimeMs";
    private long buildTimeMs = -1L;
    private static final String timeToFirstOKRequestMsHeader = "timeToFirstOKRequestMs";
    private long timeToFirstOKRequestMs = -1L;
    private static final String timeToReloadedOKRequestHeader = "timeToReloadMs";
    private long timeToReloadedOKRequest = -1L;
    private static final String startedInMsHeader = "startedInMs";
    private long startedInMs = -1L;
    private static final String stoppedInMsHeader = "stoppedInMs";
    private long stoppedInMs = -1L;
    private static final String rssKbHeader = "RSSKb";
    private long rssKb = -1L;
    private static final String openedFilesHeader = "FDs";
    private long openedFiles = -1L;
    private static final String appHeader = "App";
    private Apps app = null;
    private static final String modeHeader = "Mode";
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
        // Quarkus is not being gratefully shutdown in Windows when running in Dev mode.
        // Reported by https://github.com/quarkusio/quarkus/issues/14647.
        if (stoppedInMs <= 0 && Commands.isThisWindows) {
            // do nothing in Windows
            return this;
        }

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
        int sections = 0;
        if (app != null) {
            h.append(appHeader);
            h.append(',');
            l.append(app);
            l.append(',');
            sections++;
        }
        if (mode != null) {
            h.append(modeHeader);
            h.append(',');
            l.append(mode);
            l.append(',');
            sections++;
        }
        if (buildTimeMs != -1L) {
            h.append(buildTimeMsHeader);
            h.append(',');
            l.append(buildTimeMs);
            l.append(',');
            sections++;
        }
        if (timeToFirstOKRequestMs != -1L) {
            h.append(timeToFirstOKRequestMsHeader);
            h.append(',');
            l.append(timeToFirstOKRequestMs);
            l.append(',');
            sections++;
        }
        if (timeToReloadedOKRequest != -1L) {
            h.append(timeToReloadedOKRequestHeader);
            h.append(',');
            l.append(timeToReloadedOKRequest);
            l.append(',');
            sections++;
        }
        if (startedInMs != -1L) {
            h.append(startedInMsHeader);
            h.append(',');
            l.append(startedInMs);
            l.append(',');
            sections++;
        }
        if (stoppedInMs != -1L) {
            h.append(stoppedInMsHeader);
            h.append(',');
            l.append(stoppedInMs);
            l.append(',');
            sections++;
        }
        if (rssKb != -1L) {
            h.append(rssKbHeader);
            h.append(',');
            l.append(rssKb);
            l.append(',');
            sections++;
        }
        if (openedFiles != -1L) {
            h.append(openedFilesHeader);
            h.append(',');
            l.append(openedFiles);
            l.append(',');
            sections++;
        }
        String header = h.toString();
        // Strip trailing ',' for CSV
        String headerCSV = header.substring(0, header.length() - 1);
        String headerMarkdown = "|" + header.replaceAll(",", "|") + "\n|" + " --- |".repeat(sections);
        String line = l.toString();
        String lineCSV = line.substring(0, line.length() - 1);
        String lineMarkdown = "|" + line.replaceAll(",", "|");
        return new Log(headerCSV, headerMarkdown, lineCSV, lineMarkdown);
    }
}
