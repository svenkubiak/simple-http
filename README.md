[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.svenkubiak/simple-http/badge.svg)](https://mvnrepository.com/artifact/de.svenkubiak/simple-http)

Simple HTTP Java Client Library
================

Zero-dependency Java HTTP client that wraps around the default HTTP Client that was introduced in Java 9 making HTTP requests in Java even simpler while covering probably the majority of the standard use-cases.


Requires Java 21.


Supports GET, POST, PUT, PATCH and DELETE.

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

Simple HTTP GET call

```
Result result = Http.get("https://github.com").send();

if (result.isValid()) {
    System.out.println(result.body());
} else {
    System.out.println(result.error());
}
```

Full API

```
Result result = Http
    .get("https://github.com")
    .header("foo", "bar")
    .timeout(Duration.of(2, SECONDS))
    .followRedirects()
    .disabledValidation()
    .send();
```