package de.svenkubiak.http;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest(httpsEnabled = true)
class HttpTests {
    public static final String REQUEST_TIMED_OUT = "request timed out";
    private static final String RESPONSE = "hello, world!";
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
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
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
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
        assertThat(result.header("x-header")).isEqualTo(uuid);
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
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
    }

    @Test
    void testDefaultTimeout(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE).withFixedDelay(11000)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(REQUEST_TIMED_OUT);
    }

    @Test
    void testTimeout(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE).withFixedDelay(20000)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl()).withTimeout(Duration.of(14, SECONDS)).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(REQUEST_TIMED_OUT);
    }

    @Test
    void testDisableVerification(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(runtime.getHttpsBaseUrl()).disableValidations().send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
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
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo("");

        //when
        result = Http.get(runtime.getHttpBaseUrl() + "/redirect").followRedirects().send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
    }

    @Test
    void testPost(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(post("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.post(runtime.getHttpBaseUrl()).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
    }

    @Test
    void testPut(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(put("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.put(runtime.getHttpBaseUrl()).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
    }

    @Test
    void testPatch(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(patch("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.patch(runtime.getHttpBaseUrl()).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
    }

    @Test
    void testDelete(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(delete("/").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.delete(runtime.getHttpBaseUrl()).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
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
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
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
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void testForm(WireMockRuntimeInfo runtime) {
        //given
        String body = UUID.randomUUID().toString();
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(post("/test-form").willReturn(ok()));

        //when
        Result result = Http.post(runtime.getHttpBaseUrl() + "/test-form").withForm(Map.of("foo", "bar")).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void testWithFailsafe(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-failsafe").willReturn(badRequest()));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe").withFailsafe(2, Duration.of(10, SECONDS)).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(400);

        //when
        result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe").send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(400);

        //when
        result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe").send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(-1);
    }
}