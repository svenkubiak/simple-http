[![Maven Central](https://img.shields.io/maven-central/v/de.svenkubiak/simple-http)](https://mvnrepository.com/artifact/de.svenkubiak/simple-http)
[![Coverage](https://sonar.svenkubiak.de/badges/simple-http)](https://sonar.svenkubiak.de/badges/simple-http)
![SemVer](https://img.shields.io/badge/SemVer-2.0.0-green)
[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-%F0%9F%8D%BA-yellow)](https://buymeacoffee.com/svenkubiak)

Real Simple HTTP Java Client Library
================

Zero-dependency HTTP client that wraps around the default Java HTTP Client which was introduced in Java 9, making HTTP requests in Java even simpler while covering probably the majority of the standard use-cases.

1.x requires Java 21.

2.x requires Java 25.

3.x requires Java 25.

Supports GET, POST, PUT, PATCH and DELETE. Sync requests only.

Usage
------------------

Add the simple-http dependency to your pom.xml:

```
<dependency>
    <groupId>de.svenkubiak</groupId>
    <artifactId>simple-http</artifactId>
    <version>x.x.x</version>
</dependency>
```

Examples
------------------

HTTP GET call

```
var result = Http.get("https://github.com").send();

if (result.isValid()) {
    System.out.println(result.body());
} else {
    System.out.println(result.error());
}
```

HTTP Form POST

```
var result = Http
    .post("https://mydomain.com")
    .withForm(Map.of("username", "foo", "password", "bar"))
    .send();
```

Sending JSON with additional header

```
String json = ...
var result = Http
    .post("https://mydomain.com")
    .withHeader("Content-Type", "application/json")
    .withBody(json)
    .send();
```

Binary response (for example file downloads from a trusted source)

```
var result = Http
    .get("https://mydomain.com/report.pdf")
    .binaryResponse()
    .send();

if (result.isValid()) {
    byte[] data = result.binaryBody();
}
```

Custom response size limit

```
var result = Http
    .get("https://mydomain.com/large-export")
    .withMaxResponseSize(10L * 1024 * 1024) // 10 MiB
    .send();
```

`withMaxResponseSize()` requires a positive value. There is no unlimited response mode.

GET request with following redirects (development/testing only — see Security)

```
var result = Http
    .get("https://mydomain.com")
    .followRedirects()
    .send();
```

Defaults
------------------

Every request starts with these defaults unless you override them:

| Setting | Default |
|---------|---------|
| Timeout | 10 seconds |
| HTTP version | HTTP/2 (downgrades to HTTP/1.1 if needed) |
| Redirects | Not followed |
| TLS validation | Strict (system trust store) |
| Response size limit | 64 MiB (override with `withMaxResponseSize(long)`) |
| Allowed URL schemes | `http` and `https` only |

Configuration is done through the fluent methods on `Http` (for example `withTimeout`, `withProxy`, `withMaxResponseSize`). The underlying JDK `HttpClient` is not exposed directly.


Errors
------------------

`Result.status()` returns the HTTP status code on success. A value of `-1` means the request did not complete successfully (connection error, timeout, invalid URL, response size exceeded, or active failsafe).

`Result.error()` is an alias for `Result.body()` and contains error details when `status()` is `-1`. On successful HTTP responses, use `body()` for text content or `binaryBody()` when `binaryResponse()` was enabled.

```
var result = Http.get("https://mydomain.com").send();

if (result.isValid()) {
    System.out.println(result.body());
} else if (result.status() == -1) {
    System.out.println("Request failed: " + result.error());
} else {
    System.out.println("HTTP " + result.status() + ": " + result.body());
}
```

When failsafe is active, `error()` returns `"Failsafe is active; request was not sent"`.

Simple HTTP wraps the JDK HTTP client. For background on Java networking, see the [Java 25 networking documentation](https://docs.oracle.com/en/java/javase/25/core/java-networking.html).

Security
------------------

**Never use `disableAllHttpsValidations()` in production.** This method disables TLS certificate and hostname validation and makes your application vulnerable to man-in-the-middle attacks. Use it only in local development or automated tests with self-signed certificates.

**Only `http` and `https` URLs are allowed.** Other schemes such as `file://` are rejected when the request is sent.

**User-supplied URLs:** Do not enable `followRedirects()` for URLs provided by users. Redirects can reach internal endpoints (SSRF). Validate URLs in your application and block private or link-local IP ranges where possible.

**Response size:** Requests default to a 64 MiB response limit. Use `withMaxResponseSize(long)` to set an explicit limit for larger trusted responses. For large downloads, use `binaryResponse()` and handle the byte array responsibly.

**Authentication:** Pass tokens via headers (for example `Authorization: Bearer …`). The library does not log requests or responses. Avoid logging headers or bodies that contain credentials in your application code.

Thread safety
------------------

The typical pattern — create a new `Http` instance per request and call `send()` from one thread — is safe. Each request keeps its own configuration; the underlying JDK `HttpClient` instances are cached in `Utils` and are safe for concurrent use.

**Shared `Http` instances:** An `Http` object is not thread-safe for concurrent configuration (`withHeader`, `withBody`, `withTimeout`, and so on). Finish configuring an instance before calling `send()` from multiple threads, or use one instance per thread.

**Failsafe:** Failsafe counters are synchronized and safe when several threads call `send()` on the same configured `Http` instance. Creating a new `Http.get(...).withFailsafe(...).send()` on every call does **not** share failsafe state — keep one instance and call `send()` repeatedly:

```
var request = Http
    .get("https://github.com")
    .withFailsafe(3, Duration.of(5, ChronoUnit.MINUTES));

var result = request.send();
// ...
result = request.send();
```

**Shutdown:** `Http.shutdown()` stops every cached client in the JVM for this class loader. Other libraries or modules using simple-http in the same process will fail subsequent requests. Use only at application teardown (for example a servlet `contextDestroyed` hook), not inside reusable library code.

Failsafe
------------------
You can use a circuit breaker inspired failsafe. After n failed requests (= all non-2xx status) further requests are paused until a configured delay has passed.

Configure failsafe once on an `Http` instance and reuse that instance for every call that should share the circuit breaker (see Thread safety above).

When failsafe is active, `send()` returns `status() == -1` and `error()` contains `"Failsafe is active; request was not sent"`.
