package de.svenkubiak.http;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

public class Failsafe {
    private String url;
    private int errorCount;
    private int count;
    private Duration timeout;
    private LocalDateTime until;

    public Failsafe(String url, int count, Duration timeout) {
        this.url = Objects.requireNonNull(url, "url can not be null");
        this.count = count;
        this.timeout = Objects.requireNonNull(timeout, "timeout can not be null");
    }

    public static Failsafe of(String url, int count, Duration timeout) {
        return new Failsafe(url, count, timeout);
    }

    public boolean isActive() {
        return until != null && LocalDateTime.now().isBefore(until);
    }

    public void error() {
        errorCount = errorCount + 1;

        if (errorCount > count) {
            until = LocalDateTime.now().plus(timeout);
        }
    }

    public void success() {
        errorCount = 0;
        until = null;
    }
}
