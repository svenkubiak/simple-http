package de.svenkubiak.utils;

import de.svenkubiak.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

class UtilsTests {

    @Test
    void testIsSuccessCode() {
        //given
        List<Integer> successCodes = List.of(200, 201, 202, 203, 204, 205, 206, 207, 208, 226);

        //then
        successCodes.forEach((c -> Assertions.assertTrue(Utils.isSuccessCode(c))));

        //when
        int code = 302;

        //then
        Assertions.assertFalse(Utils.isSuccessCode(code));
    }

    @Test
    void testGetFormDataAsString() {
        //given
        Map<String, String> formData = Map.of("username", "foo", "password", "bar");

        //then
        String data = Utils.getFormDataAsString(formData);

        //then
        Assertions.assertTrue("username=foo&password=bar".equals(data) || "password=bar&username=foo".equals(data));
    }

    @Test
    void testHttpClient() {
        //given
        HttpClient client = null;

        //then
        client = Utils.getHttpClient(true, true);

        //then
        Assertions.assertNotNull(client);
        Assertions.assertInstanceOf(HttpClient.class, client);
    }
}
