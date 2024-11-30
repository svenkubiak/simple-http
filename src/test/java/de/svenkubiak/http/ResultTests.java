package de.svenkubiak.http;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTests {
    @Test
    void testWithBody() {
        //given
        String body = UUID.randomUUID().toString();

        //when
        Result result = Result.create().withBody(body);

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(body);

        //given
        body = null;

        //when
        result = Result.create().withBody(body);

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo("");
    }

    @Test
    void testWithError() {
        //given
        String body = UUID.randomUUID().toString();

        //when
        Result result = Result.create().withBody(body);

        //then
        assertThat(result).isNotNull();
        assertThat(result.error()).isEqualTo(body);
    }

    @Test
    void testWithStatus() {
        //given
        int status = 200;

        //when
        Result result = Result.create().withStatus(status);

        //when
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(status);
    }

    @Test
    void testWithHeader() {
        //given
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        //when
        Result result = Result.create().withHeader(key, value);

        //when
        assertThat(result).isNotNull();
        assertThat(result.header(key)).isEqualTo(value);
    }

    @Test
    void testIsValid() {
        //given
        int [] expected = {200, 201}; //NOSONAR

        //when
        Result result = Result.create().withStatus(200);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isValid(expected)).isTrue();

        //when
        result = result.withStatus(201);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isValid(expected)).isTrue();

        //when
        result = result.withStatus(203);

        //then
        assertThat(result).isNotNull();
        assertThat(result.isValid(expected)).isFalse();
    }
}
