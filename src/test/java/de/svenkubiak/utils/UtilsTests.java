package de.svenkubiak.utils;

import de.svenkubiak.http.Failsafe;
import de.svenkubiak.http.Result;
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
        HttpClient client;

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
        Utils.setFailsafe(url, result);

        //then
        assertThat(result).isNotNull();
        assertThat(failsafe.getCount()).isEqualTo(1);
        assertThat(failsafe.getUntil()).isNull();
    }

    @Test
    void testClean() {
        //given
        String dirtyString = "Hello, World! @#$%^&*()_+-=[]{}|;':\",./<>?`~";

        //when
        String cleaned = Utils.clean(dirtyString);

        //then
        assertThat(cleaned).isNotNull();
        assertThat(cleaned).isEqualTo("Hello World ");
    }

    @Test
    void testCleanWithEmptyString() {
        //given
        String emptyString = "";

        //when
        String cleaned = Utils.clean(emptyString);

        //then
        assertThat(cleaned).isNotNull();
        assertThat(cleaned).isEmpty();
    }

    @Test
    void testCleanWithOnlySpecialCharacters() {
        //given
        String specialChars = "@#$%^&*()";

        //when
        String cleaned = Utils.clean(specialChars);

        //then
        assertThat(cleaned).isNotNull();
        assertThat(cleaned).isEmpty();
    }

    @Test
    void testAddFailsafe() {
        //given
        String url = "http://localhost:8080/test";
        Failsafe failsafe = Failsafe.of(3, Duration.ofSeconds(10));

        //when
        Utils.addFailsafe(url, failsafe);

        //then
        assertThat(Utils.activeFailsafe(url)).isFalse();
    }

    @Test
    void testActiveFailsafe() {
        //given
        String url = "http://localhost:8080/test-active";
        Failsafe failsafe = Failsafe.of(2, Duration.ofSeconds(5));
        Utils.addFailsafe(url, failsafe);
        Result errorResult = Result.create().withStatus(404);

        //when
        Utils.setFailsafe(url, errorResult);
        Utils.setFailsafe(url, errorResult);

        //then
        assertThat(Utils.activeFailsafe(url)).isTrue();
    }

    @Test
    void testActiveFailsafeWithSuccess() {
        //given
        String url = "http://localhost:8080/test-success";
        Failsafe failsafe = Failsafe.of(2, Duration.ofSeconds(5));
        Utils.addFailsafe(url, failsafe);
        Result errorResult = Result.create().withStatus(404);
        Result successResult = Result.create().withStatus(200);

        //when
        Utils.setFailsafe(url, errorResult);
        Utils.setFailsafe(url, successResult);

        //then
        assertThat(Utils.activeFailsafe(url)).isFalse();
    }

    @Test
    void testGetFormDataAsStringWithSpecialCharacters() {
        //given
        Map<String, String> formData = Map.of("user name", "john@doe.com", "message", "Hello & World!");

        //when
        String data = Utils.getFormDataAsString(formData);

        //then
        assertThat(data).isNotNull();
        assertThat(data).contains("user+name");
        assertThat(data).contains("john%40doe.com");
        assertThat(data).contains("Hello+%26+World%21");
    }

    @Test
    void testGetFormDataAsStringWithEmptyMap() {
        //given
        Map<String, String> formData = Map.of();

        //when
        String data = Utils.getFormDataAsString(formData);

        //then
        assertThat(data).isNotNull();
        assertThat(data).isEmpty();
    }

    @Test
    void testGetFormDataAsStringWithSingleEntry() {
        //given
        Map<String, String> formData = Map.of("key", "value");

        //when
        String data = Utils.getFormDataAsString(formData);

        //then
        assertThat(data).isNotNull();
        assertThat(data).isEqualTo("key=value");
    }

    @Test
    void testGetHttpClientDifferentCombinations() {
        //given
        boolean followRedirects1 = true;
        boolean disableValidation1 = true;
        boolean followRedirects2 = false;
        boolean disableValidation2 = false;
        boolean followRedirects3 = true;
        boolean disableValidation3 = false;
        boolean followRedirects4 = false;
        boolean disableValidation4 = true;

        //when
        HttpClient client1 = Utils.getHttpClient(followRedirects1, disableValidation1);
        HttpClient client2 = Utils.getHttpClient(followRedirects2, disableValidation2);
        HttpClient client3 = Utils.getHttpClient(followRedirects3, disableValidation3);
        HttpClient client4 = Utils.getHttpClient(followRedirects4, disableValidation4);

        //then
        assertThat(client1).isNotNull();
        assertThat(client2).isNotNull();
        assertThat(client3).isNotNull();
        assertThat(client4).isNotNull();
        assertThat(client1).isInstanceOf(HttpClient.class);
        assertThat(client2).isInstanceOf(HttpClient.class);
        assertThat(client3).isInstanceOf(HttpClient.class);
        assertThat(client4).isInstanceOf(HttpClient.class);
    }

    @Test
    void testGetHttpClientCaching() {
        //given
        boolean followRedirects = true;
        boolean disableValidation = false;

        //when
        HttpClient client1 = Utils.getHttpClient(followRedirects, disableValidation);
        HttpClient client2 = Utils.getHttpClient(followRedirects, disableValidation);

        //then
        assertThat(client1).isNotNull();
        assertThat(client2).isNotNull();
        assertThat(client1).isSameAs(client2);
    }

    @Test
    void testSetFailsafeWithErrorResult() {
        //given
        String url = "http://localhost:8080/test-error";
        Failsafe failsafe = Failsafe.of(2, Duration.ofSeconds(5));
        Utils.addFailsafe(url, failsafe);
        Result errorResult = Result.create().withStatus(500);

        //when
        Utils.setFailsafe(url, errorResult);

        //then
        assertThat(Utils.activeFailsafe(url)).isFalse();
    }

    @Test
    void testSetFailsafeWithSuccessResult() {
        //given
        String url = "http://localhost:8080/test-success-result";
        Failsafe failsafe = Failsafe.of(2, Duration.ofSeconds(5));
        Utils.addFailsafe(url, failsafe);
        Result errorResult = Result.create().withStatus(404);
        Result successResult = Result.create().withStatus(200);

        //when
        Utils.setFailsafe(url, errorResult);
        Utils.setFailsafe(url, successResult);

        //then
        assertThat(Utils.activeFailsafe(url)).isFalse();
    }

    @Test
    void testIsSuccessCodeWithEdgeCases() {
        //given
        int negativeCode = -1;
        int zeroCode = 0;
        int fourHundredCode = 400;
        int fiveHundredCode = 500;

        //when
        boolean negativeResult = Utils.isSuccessCode(negativeCode);
        boolean zeroResult = Utils.isSuccessCode(zeroCode);
        boolean fourHundredResult = Utils.isSuccessCode(fourHundredCode);
        boolean fiveHundredResult = Utils.isSuccessCode(fiveHundredCode);

        //then
        assertThat(negativeResult).isFalse();
        assertThat(zeroResult).isFalse();
        assertThat(fourHundredResult).isFalse();
        assertThat(fiveHundredResult).isFalse();
    }

    @Test
    void testActiveFailsafeWithNonExistentUrl() {
        //given
        String nonExistentUrl = "http://localhost:8080/non-existent";

        //when
        boolean isActive = Utils.activeFailsafe(nonExistentUrl);

        //then
        assertThat(isActive).isFalse();
    }
}
