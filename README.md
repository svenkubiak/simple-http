[![Maven Central](https://img.shields.io/maven-central/v/de.svenkubiak/simple-http)](https://mvnrepository.com/artifact/de.svenkubiak/simple-http)
[![Coverage](https://sonar.svenkubiak.de/badges/simple-http)](https://sonar.svenkubiak.de/badges/simple-http)
![SemVer](https://img.shields.io/badge/SemVer-2.0.0-green)
[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-%F0%9F%8D%BA-yellow)](https://buymeacoffee.com/svenkubiak)

Real Simple HTTP Java Client Library
================

Zero-dependency HTTP client that wraps around the default Java HTTP Client which was introduced in Java 9, making HTTP requests in Java even simpler while covering probably the majority of the standard use-cases.

1.x requires Java 21.

2.x requires Java 25.

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

GET request without HTTP certificate validation and following redirects

```
var result = Http
    .get("https://mydomain.com")
    .disableValidations()
    .followRedirects()
    .send();
```

As Simple HTTP is build around the default Java HTTP Client you are able to use any of the default configuration options available. See https://docs.oracle.com/en/java/javase/21/core/java-networking.html for reference.

Failsafe
------------------
Since version 1.1.0 you can use a circuit breaker inspired failsafe. You can configure that after n failed requests (=all non 2xx status) the HTTP client should not send any further requests until a certain time has passed.

```
var result = Http
    .get("https://github.com")
    .withFailsafe(3, Duration.of(5, ChronoUnit.MINUTES)) // After 3 non-successful requests, pause for 5 minutes before continuing
    .send();
```