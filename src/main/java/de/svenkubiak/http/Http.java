package de.svenkubiak.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Http {
    public static final String URL_CAN_NOT_BE_NULL = "url can not be null";
    public static final String METHOD_CAN_NOT_BE_NULL = "method can not be null";
    private final HttpClient client = HttpClient.newHttpClient();
    private final String url;
    private final String method;
    private String body = "";
    private Map<String, String> headers = new HashMap<>();
    private Duration timeout = Duration.of(10, SECONDS);

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
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .version(HttpClient.Version.HTTP_2)
                    .timeout(timeout)
                    .method(method, HttpRequest.BodyPublishers.ofString(body));

            if (!headers.isEmpty()) {
                headers.forEach(builder::header);
            }

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
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
}
