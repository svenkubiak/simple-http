package de.svenkubiak.http;

public class Result {
    private String body;

    private String error;

    private int status;

    public Result () {
    }

    public Result withBody(String body) {
        this.body = body;
        return this;
    }

    public Result withError(String error) {
        this.error = error;
        return this;
    }

    public Result withStatus(int status) {
        this.status = status;
        return this;
    }

    public String body() {
        return body;
    }

    public String error() {
        return error;
    }

    public int status() {
        return status;
    }

    public boolean isSuccess() {
        return error == null || error.isBlank();
    }
}
