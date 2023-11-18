package de.svenkubiak.http;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(httpPort = 8080)
public class HttpTests {
    private static final String RESPONSE = "hello, world!";

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
