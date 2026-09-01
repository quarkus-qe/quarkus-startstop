package io.quarkus.ts.startstop.utils;

import java.time.Duration;

public final class TimeoutMeasure {
    private final long start;
    private final long duration;

    TimeoutMeasure(long durationInNanos) {
        this.start = System.nanoTime();
        this.duration = durationInNanos;
    }

    public boolean hasPassed() {
        return (System.nanoTime() - start) >= duration;
    }

    public boolean hasTimeLeft() {
        return !hasPassed();
    }

    public long elapsedMillis() {
        return (System.nanoTime() - start) / 1_000_000L;
    }

    public Duration elapsedTime() {
        return Duration.ofNanos(System.nanoTime() - start);
    }
}
