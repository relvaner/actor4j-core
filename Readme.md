[![Build Status](https://travis-ci.org/relvaner/actor4j-core.svg?branch=master)](https://travis-ci.org/relvaner/actor4j-core)
[![Coverage Status](https://coveralls.io/repos/github/relvaner/actor4j-core/badge.svg?branch=master)](https://coveralls.io/github/relvaner/actor4j-core?branch=master)

## Actor4j - Core ##

`Actor4j` is an actor-oriented Java framework, based on the actor model. `Actor4j` uses `Akka` as a base reference implementation. `Akka` is in turn influenced by `Erlang`, especially by the supervision concept. A new thread pool architecture was designed, specially designed for the exchange of messages between the actors. In contrast to `Akka`, with `Actor4j` not every actor has its own queue, but there are several task-specific queues that are located in the assigned thread. Incoming messages are injected into the actor via the corresponding thread. Each actor is permanently assigned to a thread. With this new thread pool architecture, `Actor4j` has significantly better performance compared to `Akka`. It exists also a [specification](https://github.com/relvaner/actor4j-spec) for Actor4j.

For more information on `Actor4j`, see the following more complete [documentation](https://actor4j.io/documentation/) on `actor4j.io`.

The `Actor4j - Core` library (`v1.2.x`) has no external dependencies and is also compilable as a native image with GraalVM. Minimum requirement is currently Java 17, within the branch `java-8` you find an older Java 8 version. `ActorMessage` is now encapsulated as a Java `Record`.

## License ##
This framework is released under an open source Apache 2.0 license.

## Publications ##
D. A. Bauer and J. Mäkiö, “Actor4j: A Software Framework for the Actor Model Focusing on the Optimization of Message Passing,” AICT 2018: The Fourteenth Advanced International Conference on Telecommunications, IARIA, Barcelona, Spain 2018, pp. 125-134, [Online]. Available from: http://www.thinkmind.org/download.php?articleid=aict_2018_8_10_10087

D. A. Bauer and J. Mäkiö, "Hybrid Cloud – Architecture for Administration Shells with RAMI4.0 Using Actor4j," 2019 IEEE 17th International Conference on Industrial Informatics (INDIN), Helsinki, Finland 2019, pp. 79-86, [Online]. Available from: https://doi.org/10.1109/INDIN41052.2019.8972075

## Development State ##
This software framework is currently in a prototype state.

## Installation ##

I am currently working on a new version `1.2.x` for `Actor4j`. In the near future the new version and also the other libraries will be available as a Maven dependency, as far as possible. The entire documentation is more related to the new version `1.2.x`.

Currently you can add the following Maven dependency to your `pom.xml` file:

```xml
<dependency>
	<groupId>io.actor4j</groupId>
	<artifactId>actor4j-core</artifactId>
	<version>1.0.2</version>
</dependency>
```

or a SNAPSHOT (`v1.2.0`) with JitPack.io

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

Last updated: March 27, 2022