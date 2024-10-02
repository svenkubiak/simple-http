package de.svenkubiak.http;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.time.temporal.ChronoUnit.SECONDS;

@WireMockTest(httpsEnabled = true)
class HttpTests {
    private static final String RESPONSE = "hello, world!";
    public static final String REQUEST_TIMED_OUT = "request timed out";

    @RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(wireMockConfig().bindAddress("127.0.0.1").port(10256).httpsPort(10257))
            .build();

    @Test
    void testGet(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void testResponseHeader(WireMockRuntimeInfo runtime) {
        //given
        String uuid = UUID.randomUUID().toString();
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE).withHeader("x-header", uuid)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
        Assertions.assertEquals(uuid, result.header("x-header"));
    }

    @Test
    void testRequestHeader(WireMockRuntimeInfo runtime) {
        //given
        String uuid = UUID.randomUUID().toString();
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).withHeader("Authorization", uuid).send();

        //then
        verify(
                getRequestedFor(urlEqualTo("/"))
                        .withHeader("Authorization", equalTo(uuid))
        );
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void testDefaultTimeout(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE).withFixedDelay(11000)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(REQUEST_TIMED_OUT, result.body());
    }

    @Test
    void testTimeout(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE).withFixedDelay(20000)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).withTimeout(Duration.of(14, SECONDS)).send();

        //then
        Assertions.assertEquals(REQUEST_TIMED_OUT, result.body());
    }

    @Test
    void testDisableVerification(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(runtime.getHttpsBaseUrl()).disableValidations().send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void testFollowRedirects(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/redirect").willReturn(temporaryRedirect("/")));
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/redirect").send();

        //then
        Assertions.assertEquals("", result.body());

        //when
        result = Http.get(runtime.getHttpBaseUrl() + "/redirect").followRedirects().send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void testPost(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(post("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.post(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void testPut(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(put("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.put(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void testPatch(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(patch("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.patch(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void testDelete(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(delete("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.delete(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void testHttpVersion(WireMockRuntimeInfo runtime) {
        //given
        HttpClient.Version version = HttpClient.Version.HTTP_1_1;
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-version").willReturn(ok()));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/test-version").withVersion(version).send();

        //then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testBody(WireMockRuntimeInfo runtime) {
        //given
        String body = UUID.randomUUID().toString();
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-body").willReturn(ok()));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/test-body").withBody(body).send();

        //then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testWithFailsafe(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-failsafe").willReturn(badRequest()));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe").withFailsafe(2, Duration.of(10, SECONDS)).send();

        //then
        Assertions.assertEquals(400, result.status());

        //when
        result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe").withFailsafe(2, Duration.of(10, SECONDS)).send();

        //then
        Assertions.assertEquals(400, result.status());

        //when
        result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe").withFailsafe(2, Duration.of(10, SECONDS)).send();

        //then
        Assertions.assertEquals(-1, result.status());
    }
}