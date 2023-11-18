package de.svenkubiak.http;

import de.svenkubiak.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Result {
    private Map<String, String> headers = new HashMap<>();
    private String body = "";
    private int status = -1;
    public Result () {}

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

    public String body() {
        return body;
    }

    public String header(String key) {
        return headers.get(key);
    }

    public String error() { return body; }

    public int status() {
        return status;
    }

    public boolean isValid() {
        return Utils.isSuccessCode(status);
    }

    public boolean isValid(int... expectedStatus) {
        return Arrays.stream(expectedStatus).anyMatch(s -> s == status);
    }
}
