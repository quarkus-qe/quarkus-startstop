package io.quarkus.ts.startstop.utils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class Timeout {
    private static final double MULTIPLIER = Double.parseDouble(System.getProperty("ts.timeout.multiplier", "1.0"));

    public static Timeout zero() {
        return new Timeout(0);
    }

    public static Timeout of(Duration duration) {
        return new Timeout(duration.toNanos());
    }

    public static Timeout ofMinutes(int minutes) {
        return new Timeout(TimeUnit.MINUTES.toNanos(minutes));
    }

    public static Timeout ofSeconds(int seconds) {
        return new Timeout(TimeUnit.SECONDS.toNanos(seconds));
    }

    public static Timeout ofMillis(int millis) {
        return new Timeout(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    private final long nanos;

    private Timeout(long nanos) {
        if (nanos < 0) {
            throw new IllegalArgumentException("Timeout must not be negative");
        }
        this.nanos = (long) (MULTIPLIER * nanos);
    }

    public long toSeconds() {
        return TimeUnit.NANOSECONDS.toSeconds(nanos);
    }

    public long toMillis() {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    public long toNanos() {
        return nanos;
    }

    public Duration toDuration() {
        return Duration.ofNanos(nanos);
    }

    public TimeoutMeasure measure() {
        return new TimeoutMeasure(nanos);
    }

    @Override
    public String toString() {
        if (nanos <= 0) {
            return "0ms";
        }

        long totalMillis = nanos / 1_000_000L;
        if (totalMillis == 0) {
            return "0ms";
        }

        long minutes = totalMillis / 60_000L;
        long seconds = (totalMillis % 60_000L) / 1_000L;
        long millis = totalMillis % 1_000L;

        StringBuilder result = new StringBuilder();
        if (minutes > 0) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0) {
            result.append(seconds).append("s ");
        }
        if (millis > 0 || result.isEmpty()) {
            result.append(millis).append("ms");
        }
        return result.toString().trim();
    }
}
