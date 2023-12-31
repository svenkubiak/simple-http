[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.svenkubiak/simple-http/badge.svg)](https://mvnrepository.com/artifact/de.svenkubiak/simple-http)

Simple HTTP Java Client Library
================

Zero-dependency HTTP client that wraps around the default Java HTTP Client that was introduced in Java 9 making HTTP requests in Java even simpler while covering probably the majority of the standard use-cases.


Requires Java 21.


Supports GET, POST, PUT, PATCH and DELETE. Sync calls only.

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
Result result = Http.get("https://github.com").send();

if (result.isValid()) {
    System.out.println(result.body());
} else {
    System.out.println(result.error());
}
```

Form post

```
Result result = Http
    .post("https://mydomain.com")
    .withForm(Map.of("username", "foo", "password", "bar"))
    .send();
```

Sending JSON with additional header

```
String json = ...
Result result = Http
    .post("https://mydomain.com")
    .withHeader("Content-Type", "application/json")
    .withBody(json)
    .send();
```

GET request without HTTP certificate validation and following redirects

```
Result result = Http
    .get("https://mydomain.com")
    .disableValidations()
    .followRedirects()
    .send();
```

As simple HTTP is build around the Java Http client you are able to use any of the default configuration options available. See https://docs.oracle.com/en/java/javase/21/core/java-networking.html for reference.