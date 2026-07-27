package de.svenkubiak.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class FailsafeTests {
    @Test
    void testError() {
        //given
        Failsafe failsafe = Failsafe.of(3, Duration.of(5, ChronoUnit.SECONDS));

        //then
        assertThat(failsafe).isNotNull();
        assertThat(failsafe.getCount()).isEqualTo(1);
        assertThat(failsafe.getUntil()).isNull();

        //when
        failsafe.error();

        //then
        assertThat(failsafe).isNotNull();
        assertThat(failsafe.getCount()).isEqualTo(2);
        assertThat(failsafe.getUntil()).isNull();
    }

    @Test
    void testSuccess() {
        //given
        Failsafe failsafe = Failsafe.of(2, Duration.of(5, ChronoUnit.SECONDS));

        //then
        assertThat(failsafe).isNotNull();
        assertThat(failsafe.getUntil()).isNull();
        assertThat(failsafe.getCount()).isEqualTo(1);
        assertThat(failsafe.isActive()).isFalse();

        //when
        failsafe.error();

        //then
        assertThat(failsafe).isNotNull();
        assertThat(failsafe.getUntil()).isNull();
        assertThat(failsafe.getCount()).isEqualTo(2);
        assertThat(failsafe.isActive()).isFalse();

        //when
        failsafe.error();

        //then
        assertThat(failsafe).isNotNull();
        assertThat(failsafe.getUntil()).isNotNull();
        assertThat(failsafe.getCount()).isEqualTo(3);
        assertThat(failsafe.isActive()).isTrue();

        //when
        failsafe.success();

        //then
        assertThat(failsafe).isNotNull();
        assertThat(failsafe.getUntil()).isNull();
        assertThat(failsafe.getCount()).isEqualTo(1);
        assertThat(failsafe.isActive()).isFalse();
    }

    @Test
    void testConcurrentErrors() throws InterruptedException {
        Failsafe failsafe = Failsafe.of(100, Duration.of(5, ChronoUnit.SECONDS));
        int threads = 20;
        var latch = new CountDownLatch(threads);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        failsafe.error();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        assertThat(failsafe.getCount()).isEqualTo(threads + 1);
    }
}
