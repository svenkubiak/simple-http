package de.svenkubiak.http;

import de.svenkubiak.utils.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Http {
    public static final String URL_CAN_NOT_BE_NULL = "url can not be null";
    public static final String METHOD_CAN_NOT_BE_NULL = "method can not be null";
    private final String url;
    private final String method;
    private final Map<String, String> headers = new HashMap<>();
    private String body = "";
    private Duration timeout = Duration.of(10, SECONDS);
    private boolean followRedirects;
    private boolean disableValidation;

    private Http(String url, String method) {
        this.url = Objects.requireNonNull(url, URL_CAN_NOT_BE_NULL);
        this.method = Objects.requireNonNull(method, METHOD_CAN_NOT_BE_NULL);
    }
    public static Http get(String url) {
        Objects.requireNonNull(url, URL_CAN_NOT_BE_NULL);
        return new Http(url, "GET");
    }

    public static Http post(String url) {
        Objects.requireNonNull(url, URL_CAN_NOT_BE_NULL);
        return new Http(url, "POST");
    }

    public static Http put(String url) {
        Objects.requireNonNull(url, URL_CAN_NOT_BE_NULL);
        return new Http(url, "PUT");
    }

    public static Http patch(String url) {
        Objects.requireNonNull(url, URL_CAN_NOT_BE_NULL);
        return new Http(url, "PATCH");
    }

    public static Http delete(String url) {
        Objects.requireNonNull(url, URL_CAN_NOT_BE_NULL);
        return new Http(url, "DELETE");
    }

    public Result send() {
        Result result = new Result();
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        if (followRedirects) {
            clientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
        }

        if (disableValidation) {
            clientBuilder.sslContext(Utils.getSSLContext());
        }

        try (HttpClient httpClient = clientBuilder.build()) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(timeout)
                    .method(method, HttpRequest.BodyPublishers.ofString(body));

            if (!headers.isEmpty()) {
                headers.forEach(requestBuilder::header);
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            result.withBody(response.body()).withStatus(response.statusCode());
        } catch (Exception e) {
            result.withError(e.getMessage());
        }

        return result;
    }

    public Http header(String key, String value) {
        Objects.requireNonNull(key, "key can not be null");
        Objects.requireNonNull(value, "value can not be null");

        headers.put(key, value);
        return this;
    }

    public Http timeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout can not be null");
        return this;
    }

    public Http body(String body) {
        this.body = Objects.requireNonNull(body, "body can not be null");
        return this;
    }

    public Http followRedirects() {
        this.followRedirects = true;
        return this;
    }

    public Http disableValidation() {
        this.disableValidation = true;
        return this;
    }
}