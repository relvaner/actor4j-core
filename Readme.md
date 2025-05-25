[![Build Status](https://travis-ci.org/relvaner/actor4j-core.svg?branch=master)](https://travis-ci.org/relvaner/actor4j-core)
[![Coverage Status](https://coveralls.io/repos/github/relvaner/actor4j-core/badge.svg?branch=master)](https://coveralls.io/github/relvaner/actor4j-core?branch=master)

## Actor4j - Core ##

For more information on `Actor4j`, see the following more complete [documentation](https://actor4j.io/documentation/) on `actor4j.io`.

## Status of Development ##

Most of the `Actor4j - Core` library (`v2.1.x`) has no external dependencies (except runtime-extended) and is also compilable as a native image with GraalVM. The minimum requirement is currently Java 21. Within the branch `java-8`, you find an older Java 8 version. `ActorMessage` is encapsulated as a Java `Record`. The `Actor4j - Core` library is now separated into `sdk` and `runtime` to provide more runtimes. A [specification](https://github.com/relvaner/actor4j-spec) exists for the core part of Actor4j (default runtime).

## Installation ##

I am working on a new version, `2.x` for `Actor4j`. In the future, the new version and other libraries will be available as a Maven dependency. The entire documentation is more related to the new version `2.1.x`.

Currently, you can add the following Maven dependency to your `pom.xml` file (not recomended):

```xml
<dependency>
	<groupId>io.actor4j</groupId>
	<artifactId>actor4j-core</artifactId>
	<version>1.0.2</version>
</dependency>
```

or a SNAPSHOT (`v2.1.0`) with JitPack.io

```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>io.actor4j</groupId>
		<artifactId>actor4j-core</artifactId>
		<version>master-SNAPSHOT</version>
	</dependency>
</dependencies>
```

Last updated: January 30, 2023