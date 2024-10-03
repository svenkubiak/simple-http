package de.svenkubiak.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ResultTests {
    @Test
    void testWithBody() {
        //given
        String body = UUID.randomUUID().toString();
        Result result = new Result();
        result.withBody(body);

        //then
        Assertions.assertEquals(result.body(), body);

        //given
        body = null;
        result = new Result();
        result.withBody(body);

        //then
        Assertions.assertEquals("", body);
    }

    @Test
    void testWithError() {
        //given
        String body = UUID.randomUUID().toString();
        Result result = new Result();
        result.withBody(body);

        //when
        Assertions.assertEquals(result.error(), body);
    }

    @Test
    void testyIsValid() {
        //given
        int [] expected = {200, 201}; //NOSONAR
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
