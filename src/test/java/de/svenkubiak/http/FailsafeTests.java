package de.svenkubiak.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class FailsafeTests {
    @Test
    void testError() {
        //given
        Failsafe failsafe = Failsafe.of(3, Duration.of(5, ChronoUnit.SECONDS));

        //then
        Assertions.assertEquals(1, failsafe.getCount());
        Assertions.assertNull(failsafe.getUntil());

        //when
        failsafe.error();

        //then
        Assertions.assertEquals(2, failsafe.getCount());
        Assertions.assertNull(failsafe.getUntil());
    }

    @Test
    void testSuccess() {
        //given
        Failsafe failsafe = Failsafe.of(2, Duration.of(5, ChronoUnit.SECONDS));

        //then
        Assertions.assertEquals(1, failsafe.getCount());
        Assertions.assertNull(failsafe.getUntil());
        Assertions.assertFalse(failsafe.isActive());

        //when
        failsafe.error();

        //then
        Assertions.assertEquals(2, failsafe.getCount());
        Assertions.assertNull(failsafe.getUntil());
        Assertions.assertFalse(failsafe.isActive());

        //when
        failsafe.error();

        //then
        Assertions.assertEquals(3, failsafe.getCount());
        Assertions.assertNotNull(failsafe.getUntil());
        Assertions.assertTrue(failsafe.isActive());

        //when
        failsafe.success();

        //then
        Assertions.assertEquals(1, failsafe.getCount());
        Assertions.assertNull(failsafe.getUntil());
        Assertions.assertFalse(failsafe.isActive());
    }
}
