package de.svenkubiak.http;

import de.svenkubiak.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Http {
    private static final Object CLIENT_LOCK = new Object();
    private static final Map<String, HttpClient> HTTP_CLIENTS = new ConcurrentHashMap<>(8, 0.9f, 1);
    private static final Executor EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final long DEFAULT_MAX_RESPONSE_SIZE = 64L * 1024 * 1024; //Default maximum response body size: 64 MiB.
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
     * Shuts down all cached JDK {@link HttpClient} instances held by this library.
     * <p>
     * The cache is JVM-wide and shared by every caller in the same class loader.
     * Do not call this in a library or shared runtime unless you intend to stop
     * HTTP traffic for all simple-http users in that JVM.
     */
    public static void shutdown() {
        synchronized (CLIENT_LOCK) {
            HTTP_CLIENTS.values().forEach(HttpClient::shutdownNow);
            HTTP_CLIENTS.clear();
        }
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
     * @param threshold The threshold for the failsafe; must be positive
     * @param delay The delay until the next request
     * @return The Http instance
     * @throws IllegalArgumentException if {@code threshold} is zero or negative
     */
    public Http withFailsafe(int threshold, Duration delay) {
        Objects.requireNonNull(delay, "delay can not be null");
        this.failsafe = Failsafe.of(threshold, delay);

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
     * @param maxBytes Maximum response size in bytes; must be positive
     * @return The Http instance
     * @throws IllegalArgumentException if {@code maxBytes} is zero or negative
     */
    public Http withMaxResponseSize(long maxBytes) {
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes must be positive");
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
        if (failsafe != null && failsafe.isActive()) {
            return Utils.blockedByFailsafe(result);
        }

        var httpClient = getHttpClient(followRedirects, disableValidation, proxy);
        try {
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(Utils.toAllowedUri(url))
                    .timeout(timeout)
                    .version(version)
                    .method(method, HttpRequest.BodyPublishers.ofString(body));

            if (!headers.isEmpty()) {
                headers.forEach(requestBuilder::header);
            }

            HttpResponse<InputStream> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());

            response
                    .headers()
                    .map()
                    .forEach((key, value) -> result.withHeader(key, value.getFirst()));

            try (InputStream inputStream = response.body()) {
                byte[] data = Utils.readLimited(inputStream, maxResponseSize);
                result.withStatus(response.statusCode());
                if (binaryResponse) {
                    result.withBinaryBody(data);
                } else {
                    result.withBody(new String(data, StandardCharsets.UTF_8));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String message = e.getMessage();
            if (message != null && !message.isBlank()) {
                result.withBody(Utils.clean(message));
            }
        } catch (IOException | URISyntaxException e) {
            String message = e.getMessage();
            if (message != null && !message.isBlank()) {
                result.withBody(Utils.clean(message));
            }
        }

        if (failsafe != null) {
            if (result.isValid()) {
                failsafe.success();
            } else {
                failsafe.error();
            }
        }

        return result;
    }

    private static HttpClient getHttpClient(boolean followRedirects, boolean disableValidation, InetSocketAddress proxy) {
        var key = String.valueOf(followRedirects) + disableValidation;

        if (proxy != null) {
            key = key + proxy.getHostString() + ":" + proxy.getPort();
        }

        synchronized (CLIENT_LOCK) {
            return HTTP_CLIENTS.compute(key, (cacheKey, existing) -> {
                if (existing != null && !existing.isTerminated()) {
                    return existing;
                }

                var clientBuilder = HttpClient.newBuilder().executor(EXECUTOR);

                if (followRedirects) {
                    clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
                }

                if (disableValidation) {
                    Utils.applyDisableValidation(clientBuilder);
                }

                if (proxy != null) {
                    clientBuilder.proxy(ProxySelector.of(proxy));
                }

                return clientBuilder.build();
            });
        }
    }
}