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
    private final String url;
    private final String method;
    private final Map<String, String> headers = new HashMap<>();
    private String body = "";
    private Duration timeout = Duration.of(10, SECONDS);
    HttpClient.Version version = HttpClient.Version.HTTP_2;
    private boolean followRedirects;
    private boolean disableValidation;

    private Http(String url, String method) {
        this.url = Objects.requireNonNull(url, "url can not be null");
        this.method = Objects.requireNonNull(method, "method can not be null");
    }

    /**
     * Creates a new GET request to the given URL with a default timeout
     * of 10 seconds, HTTP/2 with a downgrade to HTTP/1.1 (if supported by the server),
     * not following redirects and strict HTTPS certificate validation
     *
     * @param url The url to call
     * @return The Http instance
     */
    public static Http get(String url) {
        return new Http(url, "GET");
    }

    /**
     * Creates a new POST request to the given URL with a default timeout
     * of 10 seconds, HTTP/2 with a downgrade to HTTP/1.1 (if supported by the server),
     * not following redirects and strict HTTPS certificate validation
     *
     * @param url The url to call
     * @return The Http instance
     */
    public static Http post(String url) {
        return new Http(url, "POST");
    }

    /**
     * Creates a new PUT request to the given URL with a default timeout
     * of 10 seconds, HTTP/2 with a downgrade to HTTP/1.1 (if supported by the server),
     * not following redirects and strict HTTPS certificate validation
     *
     * @param url The url to call
     * @return The Http instance
     */
    public static Http put(String url) {
        return new Http(url, "PUT");
    }

    /**
     * Creates a new PATCH request to the given URL with a default timeout
     * of 10 seconds, HTTP/2 with a downgrade to HTTP/1.1 (if supported by the server),
     * not following redirects and strict HTTPS certificate validation
     *
     * @param url The url to call
     * @return The Http instance
     */
    public static Http patch(String url) {
        return new Http(url, "PATCH");
    }

    /**
     * Creates a new DELETE request to the given URL with a default timeout
     * of 10 seconds, HTTP/2 with a downgrade to HTTP/1.1 (if supported by the server),
     * not following redirects and strict HTTPS certificate validation
     *
     * @param url The url to call
     * @return The Http instance
     */
    public static Http delete(String url) {
        return new Http(url, "DELETE");
    }

    /**
     * Adds an additional header to the HTTP request
     *
     * @param key The key of the HTTP header
     * @param value The value of the HTTP header
     * @return The Http instance
     */
    public Http withHeader(String key, String value) {
        Objects.requireNonNull(key, "key can not be null");
        Objects.requireNonNull(value, "value can not be null");

        headers.put(key, value);
        return this;
    }

    /**
     * Sets the timeout of the request. Defaults to 10 seconds
     *
     * @param timeout The timeout to set
     * @return The Http instance
     */
    public Http withTimeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout can not be null");
        return this;
    }

    /**
     * Sets the HTTP version to use. Defaults to HTTP/2
     *
     * @param version The version to set
     * @return The Http instance
     */
    public Http withVersion(HttpClient.Version version) {
        this.version = Objects.requireNonNull(version, "version can not be null");
        return this;
    }

    /**
     * Sets the body of the request
     *
     * @param body The body to set
     * @return The Http instance
     */
    public Http withBody(String body) {
        setBody(body);
        return this;
    }

    /**
     * Adds the given form data to the request while also setting
     * content-type to "application/x-www-form-urlencoded"
     *
     * @param formData The form data
     * @return The Http instance
     */
    public Http withForm(Map<String, String> formData) {
        setBody(Utils.getFormDataAsString(formData));
        withHeader("Content-Type", "application/x-www-form-urlencoded");
        return this;
    }

    /**
     * Enables following of redirects
     * @return The Http instance
     */
    public Http followRedirects() {
        this.followRedirects = true;
        return this;
    }

    /**
     * Disables all HTTPS certificate validation
     * @return The Http instance
     */
    public Http disableValidation() {
        this.disableValidation = true;
        return this;
    }

    private void setBody(String body) {
        Objects.requireNonNull(body, "body can not be null");
        if (this.body == null || this.body.isEmpty()) {
            this.body = body;
        }
    }

    public Result send() {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        clientBuilder.version(version);

        if (followRedirects) {
            clientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
        }

        if (disableValidation) {
            clientBuilder.sslContext(Utils.getSSLContext());
        }

        Result result = new Result();
        try (HttpClient httpClient = clientBuilder.build()) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(timeout)
                    .method(method, HttpRequest.BodyPublishers.ofString(body));

            if (!headers.isEmpty()) {
                headers.forEach(requestBuilder::header);
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            response
                    .headers()
                    .map()
                    .forEach((key, value) -> result.withHeader(key, value.getFirst()));

            result
                    .withBody(response.body())
                    .withStatus(response.statusCode());
        } catch (Exception e) {
            result.withBody(e.getMessage());
        }

        return result;
    }
}