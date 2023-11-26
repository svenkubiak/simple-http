package de.svenkubiak.utils;

import de.svenkubiak.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.Map;

class UtilsTests {

    @Test
    void TestIsSuccessCode() {
        //given
        List<Integer> successCodes = List.of(200, 201, 202, 203, 204, 205, 206, 207, 208, 226);

        //then
        successCodes.stream().forEach((c -> Assertions.assertTrue(Utils.isSuccessCode(c))));

        //when
        int code = 302;

        //then
        Assertions.assertFalse(Utils.isSuccessCode(code));
    }

    @Test
    void TestGetSSLContext() {
        //given
        SSLContext sslContext = null;

        //then
        sslContext = Utils.getSSLContext();

        //then
        Assertions.assertNotNull(sslContext);
    }

    @Test
    void TestGetFormDataAsStrng() {
        //given
        Map<String, String> formData = Map.of("username", "foo");

        //then
        String data = Utils.getFormDataAsString(formData);

        //then
        Assertions.assertEquals("username=foo", data);
    }
}
