package de.svenkubiak.http;

import de.svenkubiak.utils.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable outcome of an {@link Http#send()} call.
 */
public final class Result {
    private final Map<String, String> headers;
    private final String body;
    private final byte[] binaryBody;
    private final int status;

    private Result(int status, String body, byte[] binaryBody, Map<String, String> headers) {
        this.status = status;
        this.body = (body == null || body.isEmpty()) ? "" : body;
        this.binaryBody = binaryBody == null ? null : Arrays.copyOf(binaryBody, binaryBody.length);
        this.headers = Collections.unmodifiableMap(new HashMap<>(
                Objects.requireNonNull(headers, "headers can not be null")));
    }

    static Result of(int status, String body, byte[] binaryBody, Map<String, String> headers) {
        return new Result(status, body, binaryBody, headers);
    }

    static Result failed(String message) {
        return new Result(-1, message, null, Map.of());
    }

    /**
     * @return The body of the HTTP response, or the failure text when {@link #status()} is {@code -1}
     */
    public String body() {
        return body;
    }

    /**
     * @return A copy of the binary response body, or {@code null} if none was set
     */
    public byte[] binaryBody() {
        return binaryBody == null ? null : Arrays.copyOf(binaryBody, binaryBody.length);
    }

    /**
     * Tries to get the value of a header based on the given key
     *
     * @param key The key of the header
     * @return The value of the header or null if not present
     */
    public String header(String key) {
        return headers.get(key);
    }

    /**
     * @return The HTTP status of the request or -1 if establishing a connection failed
     */
    public int status() {
        return status;
    }

    /**
     * @return True if the HTTP status matched any 2xx status code, false otherwise
     */
    public boolean isSuccess() {
        return Utils.isSuccessCode(status);
    }

    /**
     * Checks the HTTP status of the response against the given expected status
     *
     * @param expectedStatus One or more expected HTTP status
     * @return True if the HTTP status of the request matches any of the expected status
     */
    public boolean hasStatus(int... expectedStatus) {
        return Arrays.stream(expectedStatus).anyMatch(expected -> expected == status);
    }
}
