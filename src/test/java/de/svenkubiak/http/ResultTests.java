package de.svenkubiak.http;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

public class ResultTests {
    @Test
    void testWithBody() {
        //given
        String body = UUID.randomUUID().toString();
        Result result = new Result();
        result.withBody(body);

        //when
        Assertions.assertEquals(result.body(), body);
    }

    @Test
    void testyIsValid() {
        //given
        int [] expected = {200, 201};
        Result result = new Result();

        //when
        result.withStatus(200);

        //then
        Assertions.assertTrue(result.isValid(expected));

        //when
        result.withStatus(201);

        //then
        Assertions.assertTrue(result.isValid(expected));

        //when
        result.withStatus(203);

        //then
        Assertions.assertFalse(result.isValid(expected));
    }
}
