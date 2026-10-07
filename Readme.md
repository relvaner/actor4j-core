[![Maven Central](https://img.shields.io/maven-central/v/io.actor4j/actor4j-core-runtime?include_prereleases)](https://central.sonatype.com/namespace/io.actor4j)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

## Actor4j - Core ##

Lightweight actor model runtime for Java 21+. No external dependencies, GraalVM native-image ready.
**One API, three runtimes**: choose the execution model that fits your workload, without changing your code.

| Runtime | Artifact | Best for |
|---|---|---|
| Thread-bound (default) | `actor4j-core-runtime` | actors that can be grouped, high intra-thread messaging |
| Classic | `actor4j-core-runtime-classic` | heavy coordination between many actors (work-stealing) |
| Loom | `actor4j-core-runtime-loom` | I/O-bound actors (virtual threads) |

See *Multi-Runtime Actor Model Implementation and Benchmarks* (IEEE IECON 2025) for the evaluation.

### Installation

**Developing actors or a library?** Depend only on the SDK. Your code stays runtime-independent:

```xml
<dependency>
    <groupId>io.actor4j</groupId>
    <artifactId>actor4j-core-sdk</artifactId>
    <version>2.4.0-beta.3</version>
</dependency>
```

**Running an application?** Add the runtime of your choice (it includes the SDK transitively):

```xml
<dependency>
    <groupId>io.actor4j</groupId>
    <artifactId>actor4j-core-runtime</artifactId> <!-- or -classic, -loom -->
    <version>2.4.0-beta.3</version>
</dependency>
```

Actors written against the SDK run unchanged on every runtime, so you can develop first and choose
(or switch) the runtime later, based on your workload.

#### Snapshots (JitPack)

The latest development version from `master` is available via JitPack:

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
### Hello World

```java
ActorSystem system = ActorSystem.create(ActorRuntime.factory());

ActorId greeter = system.addActor(() -> new Actor() {
    @Override
    public void receive(ActorMessage<?> message) {
        System.out.println("Hello, " + message.value() + "!");
    }
});

system.start();
system.send(ActorMessage.create("World", 0, system.SYSTEM_ID(), greeter));
```

### Learn more
- [Documentation](https://actor4j.io/documentation/)
- [Specification](https://github.com/relvaner/actor4j-spec)
- [Publications](https://actor4j.io/publications/)

Last updated: October 7, 2026
