package de.svenkubiak.utils;

import de.svenkubiak.http.Failsafe;
import de.svenkubiak.http.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTests {

    @Test
    void testIsSuccessCode() {
        //given
        List<Integer> successCodes = List.of(200, 201, 202, 203, 204, 205, 206, 207, 208, 226);

        //then
        successCodes.forEach((c -> assertThat(Utils.isSuccessCode(c)).isTrue()));

        //when
        int code = 302;

        //then
        assertThat(Utils.isSuccessCode(code)).isFalse();
    }

    @Test
    void testGetFormDataAsString() {
        //given
        Map<String, String> formData = Map.of("username", "foo", "password", "bar");

        //then
        String data = Utils.getFormDataAsString(formData);

        //then
        assertThat("username=foo&password=bar".equals(data) || "password=bar&username=foo".equals(data)).isTrue();
    }

    @Test
    void testHttpClient() {
        //given
        HttpClient client = null;

        //then
        client = Utils.getHttpClient(true, true);

        //then
        assertThat(client).isNotNull();
        assertThat(client).isInstanceOf(HttpClient.class);
    }

    @Test
    void testSetFailsafe() {
        //given
        String url = "http://localhost:8080";
        Failsafe failsafe = Failsafe.of(5, Duration.ofMinutes(1));
        Result result = Result.create().withStatus(200);

        //when
        Utils.setFailsafe(url, failsafe, result);

        //then
        assertThat(result).isNotNull();
        assertThat(failsafe.getCount()).isEqualTo(1);
        assertThat(failsafe.getUntil()).isNull();
    }
}
