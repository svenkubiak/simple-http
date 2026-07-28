package de.svenkubiak.utils;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void testToAllowedUri() throws URISyntaxException {
        assertThat(Utils.toAllowedUri("https://example.com/path").toString())
                .isEqualTo("https://example.com/path");
        assertThat(Utils.toAllowedUri("http://localhost:8080").getScheme()).isEqualTo("http");
    }

    @Test
    void testToAllowedUriRejectsFileScheme() {
        assertThatThrownBy(() -> Utils.toAllowedUri("file:///etc/passwd"))
                .isInstanceOf(URISyntaxException.class)
                .hasMessageContaining("Only http and https URLs are allowed");
    }
}
