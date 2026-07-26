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
    private static final String REQUEST_TIMED_OUT = "request timed out";
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
        Result result = Http.get(runtime.getHttpsBaseUrl()).disableAllHttpsValidations().send();

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
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(post("/test-form").willReturn(ok()));

        //when
        Result result = Http.post(runtime.getHttpBaseUrl() + "/test-form").withForm(Map.of("foo", "bar")).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void testWithRequestFailsafe(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-failsafe").willReturn(badRequest()));
        var request = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe")
                .withRequestFailsafe(2, Duration.of(10, SECONDS));

        //when
        Result result = request.send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(400);

        //when
        result = request.send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(400);

        //when
        result = request.send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDeprecatedWithFailsafeByUrl(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-failsafe-deprecated").willReturn(badRequest()));
        String url = runtime.getHttpBaseUrl() + "/test-failsafe-deprecated";

        //when
        Result result = Http.get(url).withFailsafe(2, Duration.of(10, SECONDS)).send();

        //then
        assertThat(result.status()).isEqualTo(400);

        //when
        result = Http.get(url).send();

        //then
        assertThat(result.status()).isEqualTo(400);

        //when
        result = Http.get(url).send();

        //then
        assertThat(result.status()).isEqualTo(0);
    }

    @Test
    void testWithFailsafeKey(WireMockRuntimeInfo runtime) {
        //given
        String key = UUID.randomUUID().toString();
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-failsafe-key").willReturn(badRequest()));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe-key")
                .withFailsafe(key, 2, Duration.of(10, SECONDS))
                .send();

        //then
        assertThat(result.status()).isEqualTo(400);

        //when
        result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe-key")
                .withFailsafe(key, 2, Duration.of(10, SECONDS))
                .send();

        //then
        assertThat(result.status()).isEqualTo(400);

        //when
        result = Http.get(runtime.getHttpBaseUrl() + "/test-failsafe-key")
                .withFailsafe(key, 2, Duration.of(10, SECONDS))
                .send();

        //then
        assertThat(result.status()).isEqualTo(0);
    }

    @Test
    void testWithRequestFailsafeNotSharedAcrossInstances(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-failsafe-isolated").willReturn(badRequest()));
        String url = runtime.getHttpBaseUrl() + "/test-failsafe-isolated";

        //when
        Result first = Http.get(url).withRequestFailsafe(1, Duration.of(10, SECONDS)).send();
        Result second = Http.get(url).withRequestFailsafe(1, Duration.of(10, SECONDS)).send();

        //then
        assertThat(first.status()).isEqualTo(400);
        assertThat(second.status()).isEqualTo(400);
    }

    @Test
    void testBinaryResponse(WireMockRuntimeInfo runtime) {
        //given
        byte[] binaryData = new byte[]{0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello" in bytes
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-binary").willReturn(ok().withBody(binaryData)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/test-binary").binaryResponse().send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.binaryBody()).isNotNull();
        assertThat(result.binaryBody()).isEqualTo(binaryData);
        assertThat(result.status()).isEqualTo(200);
    }

    @Test
    void testPostWithBody(WireMockRuntimeInfo runtime) {
        //given
        String requestBody = "{\"name\":\"test\",\"value\":123}";
        String responseBody = "{\"status\":\"success\"}";
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(post("/test-post-body")
                .withRequestBody(equalTo(requestBody))
                .willReturn(ok().withBody(responseBody)));

        //when
        Result result = Http.post(runtime.getHttpBaseUrl() + "/test-post-body")
                .withBody(requestBody)
                .withHeader("Content-Type", "application/json")
                .send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(responseBody);
        assertThat(result.status()).isEqualTo(200);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void testMultipleHeaders(WireMockRuntimeInfo runtime) {
        //given
        String authToken = UUID.randomUUID().toString();
        String userAgent = "TestClient/1.0";
        String contentType = "application/json";
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-multiple-headers").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/test-multiple-headers")
                .withHeader("Authorization", "Bearer " + authToken)
                .withHeader("User-Agent", userAgent)
                .withHeader("Content-Type", contentType)
                .send();

        //then
        verify(
                getRequestedFor(urlEqualTo("/test-multiple-headers"))
                        .withHeader("Authorization", equalTo("Bearer " + authToken))
                        .withHeader("User-Agent", equalTo(userAgent))
                        .withHeader("Content-Type", equalTo(contentType))
        );
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void testWithUrl(WireMockRuntimeInfo runtime) {
        //given
        String initialUrl = runtime.getHttpBaseUrl() + "/initial";
        String newUrl = runtime.getHttpBaseUrl() + "/new";
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/initial").willReturn(notFound()));
        wireMock.register(get("/new").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get(initialUrl).withUrl(newUrl).send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
        assertThat(result.status()).isEqualTo(200);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void testWithProxy(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/proxied").willReturn(ok().withBody(RESPONSE)));

        //when
        Result result = Http.get("http://127.0.0.1:" + runtime.getHttpPort() + "/proxied")
                .withProxy("127.0.0.1", runtime.getHttpPort())
                .send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(RESPONSE);
        assertThat(result.status()).isEqualTo(200);
    }

    @Test
    void testMaxResponseSizeWithinLimit(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/limited").willReturn(ok().withBody("hello")));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/limited")
                .withMaxResponseSize(10)
                .send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo("hello");
        assertThat(result.status()).isEqualTo(200);
    }

    @Test
    void testMaxResponseSizeExceeded(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/too-large").willReturn(ok().withBody("hello, world!")));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/too-large")
                .withMaxResponseSize(5)
                .send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(-1);
        assertThat(result.body()).contains("exceeds maximum size");
    }

    @Test
    void testMaxResponseSizeBinaryExceeded(WireMockRuntimeInfo runtime) {
        //given
        byte[] binaryData = new byte[]{0x48, 0x65, 0x6C, 0x6C, 0x6F};
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/binary-limited").willReturn(ok().withBody(binaryData)));

        //when
        Result result = Http.get(runtime.getHttpBaseUrl() + "/binary-limited")
                .binaryResponse()
                .withMaxResponseSize(3)
                .send();

        //then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(-1);
        assertThat(result.body()).contains("exceeds maximum size");
    }

    @Test
    void testStatusCodes(WireMockRuntimeInfo runtime) {
        //given
        WireMock wireMock = runtime.getWireMock();
        wireMock.register(get("/test-200").willReturn(ok()));
        wireMock.register(get("/test-404").willReturn(notFound()));
        wireMock.register(get("/test-500").willReturn(serverError()));

        //when
        Result result200 = Http.get(runtime.getHttpBaseUrl() + "/test-200").send();
        Result result404 = Http.get(runtime.getHttpBaseUrl() + "/test-404").send();
        Result result500 = Http.get(runtime.getHttpBaseUrl() + "/test-500").send();

        //then
        assertThat(result200).isNotNull();
        assertThat(result200.status()).isEqualTo(200);
        assertThat(result200.isValid()).isTrue();

        assertThat(result404).isNotNull();
        assertThat(result404.status()).isEqualTo(404);
        assertThat(result404.isValid()).isFalse();

        assertThat(result500).isNotNull();
        assertThat(result500.status()).isEqualTo(500);
        assertThat(result500.isValid()).isFalse();
    }
}