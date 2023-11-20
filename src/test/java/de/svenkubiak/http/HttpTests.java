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

@WireMockTest(httpsEnabled = true, httpPort = 10256, httpsPort = 10257)
public class HttpTests {
    private static final String RESPONSE = "hello, world!";
    public static final String REQUEST_TIMED_OUT = "request timed out";

    @RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(wireMockConfig().bindAddress("127.0.0.1"))
            .build();

    @Test
    void TestGet(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void TestResponseHeader(WireMockRuntimeInfo runtime) {
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
    void TestRequestHeader(WireMockRuntimeInfo runtime) {
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
    void TestDefaultTimeout(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE).withFixedDelay(11000)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(REQUEST_TIMED_OUT, result.body());
    }

    @Test
    void TestTimeout(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE).withFixedDelay(20000)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).withTimeout(Duration.of(14, SECONDS)).send();

        //then
        Assertions.assertEquals(REQUEST_TIMED_OUT, result.body());
    }

    @Test
    void TestDisableVerification(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(runtime.getHttpsBaseUrl()).disableValidations().send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void TestFollowRedirects(WireMockRuntimeInfo runtime) {
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
    void TestPost(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(post("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.post(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void TestPut(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(put("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.put(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void TestPatch(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(patch("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.patch(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }

    @Test
    void TestDelete(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(delete("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.delete(runtime.getHttpBaseUrl()).send();

        //then
        Assertions.assertEquals(RESPONSE, result.body());
    }
}