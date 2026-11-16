package de.svenkubiak.http;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTests {
    @Test
    void testBody() {
        String body = UUID.randomUUID().toString();

        Result result = Result.of(200, body, null, Map.of());

        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(body);

        result = Result.of(-1, null, null, Map.of());

        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo("");
    }

    @Test
    void testStatus() {
        Result result = Result.of(200, "", null, Map.of());

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(200);
    }

    @Test
    void testHeader() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        Result result = Result.of(200, "", null, Map.of(key, value));

        assertThat(result).isNotNull();
        assertThat(result.header(key)).isEqualTo(value);
    }

    @Test
    void testHeadersAreCopied() {
        Map<String, String> headers = new HashMap<>();
        headers.put("k", "v");
        Result result = Result.of(200, "", null, headers);

        headers.put("k", "changed");

        assertThat(result.header("k")).isEqualTo("v");
    }

    @Test
    void testIsSuccess() {
        assertThat(Result.of(200, "", null, Map.of()).isSuccess()).isTrue();
        assertThat(Result.of(204, "", null, Map.of()).isSuccess()).isTrue();
        assertThat(Result.of(404, "", null, Map.of()).isSuccess()).isFalse();
        assertThat(Result.of(-1, "", null, Map.of()).isSuccess()).isFalse();
    }

    @Test
    void testHasStatus() {
        int[] expected = {200, 201};

        Result result = Result.of(200, "", null, Map.of());

        assertThat(result).isNotNull();
        assertThat(result.hasStatus(expected)).isTrue();

        result = Result.of(201, "", null, Map.of());

        assertThat(result).isNotNull();
        assertThat(result.hasStatus(expected)).isTrue();

        result = Result.of(203, "", null, Map.of());

        assertThat(result).isNotNull();
        assertThat(result.hasStatus(expected)).isFalse();
    }

    @Test
    void testBinaryBodyReturnsCopy() {
        byte[] original = new byte[]{1, 2, 3};
        Result result = Result.of(200, "", original, Map.of());
        original[0] = 99;

        byte[] returned = result.binaryBody();
        returned[0] = 7;

        assertThat(result.binaryBody()).containsExactly(1, 2, 3);
    }
}
