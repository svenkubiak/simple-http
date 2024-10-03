package de.svenkubiak.http;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Failsafe {
    private final int threshold;
    private final Duration delay;
    private int count = 1;
    private LocalDateTime until;

    public Failsafe(int threshold, Duration delay) {
        this.threshold = threshold;
        this.delay = Objects.requireNonNull(delay, "delay can not be null");
    }

    public static Failsafe of(int threshold, Duration timeout) {
        return new Failsafe(threshold, timeout);
    }

    public boolean isActive() {
        return until != null && LocalDateTime.now().isBefore(until);
    }

    public void error() {
        count = count + 1;
        if (count > threshold) {
            until = LocalDateTime.now().plus(delay);
        }
    }

    public void success() {
        count = 1;
        until = null;
    }

    public int getCount() {
        return count;
    }

    public LocalDateTime getUntil() {
        return until;
    }
}