package de.svenkubiak.http;

import de.svenkubiak.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Result {
    private final Map<String, String> headers = new HashMap<>();
    private String body = "";
    private int status = -1;

    public Result withBody(String body) {
        this.body = (body == null || body.isEmpty()) ? "" : body;
        return this;
    }

    public Result withStatus(int status) {
        this.status = status;
        return this;
    }

    public Result withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * @return The body of the HTTP response
     */
    public String body() {
        return body;
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
     * @return Any error that might have occurred during the connection
     */
    public String error() { return body; }

    /**
     * @return The HTTP status of the request or -1 if establishing a connection failed
     */
    public int status() {
        return status;
    }

    /**
     * @return True if the HTTP status matched any 2xx status code, false otherwise
     */
    public boolean isValid() {
        return Utils.isSuccessCode(status);
    }

    /**
     * Checks the HTTP status of the response against the given expected status
     *
     * @param expectedStatus One or more expected HTTP status
     * @return True if the HTTP status of the request matches any of the expected status
     */
    public boolean isValid(int... expectedStatus) {
        return Arrays.stream(expectedStatus).anyMatch(s -> s == status);
    }
}
