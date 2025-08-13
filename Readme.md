[![Build Status](https://travis-ci.org/relvaner/actor4j-core.svg?branch=master)](https://travis-ci.org/relvaner/actor4j-core)
[![Coverage Status](https://coveralls.io/repos/github/relvaner/actor4j-core/badge.svg?branch=master)](https://coveralls.io/github/relvaner/actor4j-core?branch=master)

## Actor4j - Core ##

For more information on `Actor4j`, see the following more complete [documentation](https://actor4j.io/documentation/) on `actor4j.io`.

## Status of Development ##

Most of the `Actor4j - Core` library (`v2.x`) has no external dependencies (except runtime-extended) and is also compilable as a native image with GraalVM. The minimum requirement is currently Java 21. Within the branch `java-8`, you find an older Java 8 version. `ActorMessage` is encapsulated as a Java `Record`. The `Actor4j - Core` library is now separated into `sdk` and `runtime` to provide more runtimes. A [specification](https://github.com/relvaner/actor4j-spec) exists for the core part of Actor4j (default runtime).

## Installation ##

The current version is `2.4`, and it is still under further development. In the future, new versions and other libraries will be available as a Maven dependency. The entire documentation is now related to version `2.4.x`. Please note that the documentation is currently incomplete and does not yet fully reflect all the features implemented.

Currently, you can add the following Maven dependencies to your pom.xml file (using your preffered runtime):

```xml
<!-- SDK -->
<dependency>
	<groupId>io.actor4j</groupId>
	<artifactId>actor4j-core-sdk</artifactId>
	<version><!-- REPLACE WITH LATEST RELEASE --></version>
</dependency>

<!-- DEFAULT RUNTIME (thread-bounded message queue) -->
<dependency>
	<groupId>io.actor4j</groupId>
	<artifactId>actor4j-core-runtime</artifactId>
	<version><!-- REPLACE WITH LATEST RELEASE --></version>
</dependency>

<!-- CLASSIC RUNTIME (actor-bounded message queue) -->
<dependency>
	<groupId>io.actor4j</groupId>
	<artifactId>actor4j-core-runtime-classic</artifactId>
	<version><!-- REPLACE WITH LATEST RELEASE --></version>
</dependency>

<!-- LOOM RUNTIME (actor-bounded message queue, using virtual threads) -->
<dependency>
	<groupId>io.actor4j</groupId>
	<artifactId>actor4j-core-runtime-loom</artifactId>
	<version><!-- REPLACE WITH LATEST RELEASE --></version>
</dependency>

or a SNAPSHOT with JitPack.io:

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

Last updated: August 13, 2025