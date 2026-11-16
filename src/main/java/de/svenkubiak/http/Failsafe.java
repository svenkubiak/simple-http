package de.svenkubiak.http;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

class Failsafe {
    private final int threshold;
    private final Duration delay;
    private int count = 1;
    private LocalDateTime until;

    Failsafe(int threshold, Duration delay) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("threshold must be positive");
        }
        this.threshold = threshold;
        this.delay = Objects.requireNonNull(delay, "delay can not be null");
    }

    static Failsafe of(int threshold, Duration timeout) {
        return new Failsafe(threshold, timeout);
    }

    synchronized boolean isActive() {
        return until != null && LocalDateTime.now().isBefore(until);
    }

    synchronized void error() {
        count = count + 1;
        if (count > threshold) {
            until = LocalDateTime.now().plus(delay);
        }
    }

    synchronized void success() {
        count = 1;
        until = null;
    }

    synchronized int getCount() {
        return count;
    }

    synchronized LocalDateTime getUntil() {
        return until;
    }
}
