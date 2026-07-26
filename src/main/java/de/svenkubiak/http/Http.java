package de.svenkubiak.http;

import de.svenkubiak.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Http {
    /** Default maximum response body size: 64 MiB. */
    public static final long DEFAULT_MAX_RESPONSE_SIZE = 64L * 1024 * 1024;

    private static final Map<String, Failsafe> SHARED_FAILSAFES = new ConcurrentHashMap<>();

    private final String method;
    private final Map<String, String> headers = new HashMap<>();
    private String url;
    private String body = "";
    private Duration timeout = Duration.of(10, SECONDS);
    private HttpClient.Version version = HttpClient.Version.HTTP_2;
    private InetSocketAddress proxy;
    private boolean followRedirects;
    private boolean disableValidation;
    private boolean binaryResponse;
    private long maxResponseSize = DEFAULT_MAX_RESPONSE_SIZE;
    private Failsafe failsafe;
    private String failsafeKey;

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
     * Closes and shuts down all Http Clients
     */
    public static void shutdown() {
        Utils.shutdown();
    }

    /**
     * Adds the url to call changing the initial value
     *
     * @param url The url to call
     * @return The Http instance
     */
    public Http withUrl(String url) {
        Objects.requireNonNull(url, "url can not be null");
        this.url = url;
        return this;
    }

    /**
     * Adds a header to the HTTP request
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
     * Adds a proxy to the HttpClient. Each request is the run through
     * the defined proxy when the HTTP request is executed
     *
     * @param host The hostname of the proxy
     * @param port The port of the proxy
     * @return The Http instance
     */
    public Http withProxy(String host, int port) {
        Objects.requireNonNull(host, "host can not be null");
        this.proxy = new InetSocketAddress(host, port);

        return this;
    }

    /**
     * Adds a failsafe scoped to this {@code Http} instance. Reuse the same instance
     * across calls to accumulate failures.
     *
     * @param threshold The threshold for the failsafe
     * @param delay The delay until the next request
     * @return The Http instance
     */
    public Http withRequestFailsafe(int threshold, Duration delay) {
        Objects.requireNonNull(delay, "delay can not be null");
        this.failsafe = Failsafe.of(threshold, delay);
        this.failsafeKey = null;

        return this;
    }

    /**
     * Adds a failsafe shared JVM-wide by URL. New {@code Http} instances for the same
     * URL share the same circuit-breaker state.
     *
     * @param threshold The threshold for the failsafe
     * @param delay The delay until the next request
     * @return The Http instance
     * @deprecated Use {@link #withRequestFailsafe(int, Duration)} for instance-local state,
     *             or {@link #withFailsafe(String, int, Duration)} to share state by an explicit key.
     *             URL-based failsafe state is JVM-global and may interfere across unrelated callers.
     */
    @Deprecated(since = "2.0.8", forRemoval = true)
    public Http withFailsafe(int threshold, Duration delay) {
        Objects.requireNonNull(delay, "delay can not be null");
        this.failsafe = null;
        this.failsafeKey = null;
        Utils.addFailsafe(url, Failsafe.of(threshold, delay));

        return this;
    }

    /**
     * Adds a failsafe shared by an explicit key across {@code Http} instances.
     * Use this when multiple callers should share the same circuit-breaker state.
     *
     * @param key A caller-defined key to group failsafe state
     * @param threshold The threshold for the failsafe
     * @param delay The delay until the next request
     * @return The Http instance
     */
    public Http withFailsafe(String key, int threshold, Duration delay) {
        Objects.requireNonNull(key, "key can not be null");
        Objects.requireNonNull(delay, "delay can not be null");
        this.failsafeKey = key;
        this.failsafe = null;
        SHARED_FAILSAFES.computeIfAbsent(key, ignored -> Failsafe.of(threshold, delay));

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
     * Enables binary response so that the content
     * response can be handled accordingly (e.g. download)
     * @return The Http instance
     */
    public Http binaryResponse() {
        this.binaryResponse = true;
        return this;
    }

    /**
     * Disables all HTTPS certificate and hostname validation.
     * Do not use in production; enables MITM attacks.
     *
     * @return The Http instance
     */
    public Http disableAllHttpsValidations() {
        this.disableValidation = true;
        return this;
    }

    /**
     * Sets the maximum allowed response body size in bytes.
     * Requests exceeding this limit fail with an error result.
     * Defaults to {@value #DEFAULT_MAX_RESPONSE_SIZE} bytes (64 MiB).
     *
     * @param maxBytes Maximum response size in bytes; 0 means unlimited
     * @return The Http instance
     */
    public Http withMaxResponseSize(long maxBytes) {
        if (maxBytes < 0) {
            throw new IllegalArgumentException("maxBytes must not be negative");
        }
        this.maxResponseSize = maxBytes;
        return this;
    }

    private void setBody(String body) {
        Objects.requireNonNull(body, "body can not be null");
        if (this.body == null || this.body.isEmpty()) {
            this.body = body;
        }
    }

    public Result send() {
        var result = Result.create();
        if (Utils.activeFailsafe(url)) {
            result.withStatus(0);
            return result;
        }

        var currentFailsafe = resolveFailsafe();
        if (currentFailsafe != null && currentFailsafe.isActive()) {
            result.withStatus(0);
            return result;
        }

        var httpClient = Utils.getHttpClient(followRedirects, disableValidation, proxy);
        try {
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(timeout)
                    .version(version)
                    .method(method, HttpRequest.BodyPublishers.ofString(body));

            if (!headers.isEmpty()) {
                headers.forEach(requestBuilder::header);
            }

            if (maxResponseSize > 0) {
                HttpResponse<InputStream> response = httpClient.send(
                        requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofInputStream());

                response
                        .headers()
                        .map()
                        .forEach((key, value) -> result.withHeader(key, value.getFirst()));

                try (InputStream inputStream = response.body()) {
                    byte[] data = readLimited(inputStream, maxResponseSize);
                    result.withStatus(response.statusCode());
                    if (binaryResponse) {
                        result.withBinaryBody(data);
                    } else {
                        result.withBody(new String(data, StandardCharsets.UTF_8));
                    }
                }
            } else if (binaryResponse) {
                HttpResponse<byte []> response = httpClient.send(
                        requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofByteArray());

                response
                        .headers()
                        .map()
                        .forEach((key, value) -> result.withHeader(key, value.getFirst()));

                result
                        .withBinaryBody(response.body())
                        .withStatus(response.statusCode());
            } else {
                HttpResponse<String> response = httpClient.send(
                        requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofString());

                response
                        .headers()
                        .map()
                        .forEach((key, value) -> result.withHeader(key, value.getFirst()));

                result
                        .withBody(response.body())
                        .withStatus(response.statusCode());
            }
        } catch (IOException | InterruptedException | URISyntaxException e) { //NOSONAR
            String message = e.getMessage();
            if (message != null && !message.isBlank()) {
                result.withBody(Utils.clean(message));
            }
        }

        if (currentFailsafe != null) {
            if (result.isValid()) {
                currentFailsafe.success();
            } else {
                currentFailsafe.error();
            }
        }

        Utils.setFailsafe(url, result);

        return result;
    }

    private Failsafe resolveFailsafe() {
        if (failsafeKey != null) {
            return SHARED_FAILSAFES.get(failsafeKey);
        }

        return failsafe;
    }

    private static byte[] readLimited(InputStream inputStream, long maxBytes) throws IOException {
        byte[] buffer = new byte[8192];
        var output = new java.io.ByteArrayOutputStream();
        int read;

        while ((read = inputStream.read(buffer)) != -1) {
            if (output.size() + read > maxBytes) {
                throw new IOException("Response body exceeds maximum size of " + maxBytes + " bytes");
            }
            output.write(buffer, 0, read);
        }

        return output.toByteArray();
    }
}