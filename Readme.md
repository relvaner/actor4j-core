[![Build Status](https://travis-ci.org/relvaner/actor4j-core.svg?branch=master)](https://travis-ci.org/relvaner/actor4j-core)
[![Coverage Status](https://coveralls.io/repos/github/relvaner/actor4j-core/badge.svg?branch=master)](https://coveralls.io/github/relvaner/actor4j-core?branch=master)

## Actor4j - Core ##

For more information on `Actor4j`, see the following more complete [documentation](https://actor4j.io/documentation/) on `actor4j.io`.

The `Actor4j - Core` library (`v2.0.x`) has no external dependencies and is also compilable as a native image with GraalVM. Minimum requirement is currently Java 17, within the branch `java-8` you find an older Java 8 version. `ActorMessage` is encapsulated as a Java `Record`. The `Actor4j - Core` library is now separated into `sdk` and `runtime` to be able to provide more runtimes in the future. It exists also a [specification](https://github.com/relvaner/actor4j-spec) for the core part of Actor4j.

## License ##
This framework is released under an open source Apache 2.0 license.

## Publications ##
D. A. Bauer and J. Mäkiö, “Actor4j: A Software Framework for the Actor Model Focusing on the Optimization of Message Passing,” AICT 2018: The Fourteenth Advanced International Conference on Telecommunications, IARIA, Barcelona, Spain 2018, pp. 125-134, [Online]. Available from: http://www.thinkmind.org/download.php?articleid=aict_2018_8_10_10087

D. A. Bauer and J. Mäkiö, "Hybrid Cloud – Architecture for Administration Shells with RAMI4.0 Using Actor4j," 2019 IEEE 17th International Conference on Industrial Informatics (INDIN), Helsinki, Finland 2019, pp. 79-86, [Online]. Available from: https://doi.org/10.1109/INDIN41052.2019.8972075

## Development State ##
This software framework is currently in a prototype state.

## Installation ##

I am currently working on a new version `2.0.x` for `Actor4j`. In the near future the new version and also the other libraries will be available as a Maven dependency, as far as possible. The entire documentation is more related to the new version `2.0.x`.

Currently you can add the following Maven dependency to your `pom.xml` file:

```xml
<dependency>
	<groupId>io.actor4j</groupId>
	<artifactId>actor4j-core</artifactId>
	<version>1.0.2</version>
</dependency>
```

or a SNAPSHOT (`v2.0.0`) with JitPack.io

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