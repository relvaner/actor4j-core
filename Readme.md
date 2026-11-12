[![Maven Central](https://img.shields.io/maven-central/v/io.actor4j/actor4j-core-runtime?include_prereleases)](https://central.sonatype.com/namespace/io.actor4j)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

## Actor4j - Core ##

Lightweight actor model framework for Java 21+: one SDK, four runtimes (default, classic, loom, hybrid). No external dependencies (except runtime-extended), GraalVM native-image ready.
**One API, four runtimes**: choose the execution model that fits your workload, without changing your code.

| Runtime | Artifact | Message queues | Best for |
|---|---|---|---|
| Default | `actor4j-core-runtime` | thread-bound | communicating actors grouped on the same thread |
| Classic | `actor4j-core-runtime-classic` | one per actor, work-stealing thread pool | any actor distribution |
| Loom | `actor4j-core-runtime-loom` | one per actor, on virtual threads | blocking I/O |
| Hybrid | `actor4j-core-runtime-hybrid` | thread-bound (default) and work-stealing pool (classic), chosen per actor | steady thread-bound work next to briefly active coordination |

See *Multi-Runtime Actor Model Implementation and Benchmarks* (IEEE IECON 2025) for the evaluation.

### Installation

**Developing actors or a library?** Depend only on the SDK. Your code stays runtime-independent:

```xml
<dependency>
    <groupId>io.actor4j</groupId>
    <artifactId>actor4j-core-sdk</artifactId>
    <version>2.5.0-beta.1</version>
</dependency>
```

**Running an application?** Add the runtime of your choice (it includes the SDK transitively):

```xml
<dependency>
    <groupId>io.actor4j</groupId>
    <artifactId>actor4j-core-runtime</artifactId> <!-- or -classic, -loom, -hybrid -->
    <version>2.5.0-beta.1</version>
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

### Hybrid runtime

The hybrid runtime combines the default and the classic runtime in one actor system: actors are thread-bound
as in the default runtime, actors chosen by a placement rule run in the work-stealing pool of the classic runtime.
This separates the work by its type, e.g. long-lived actors with steady load (actor pods, devices, groups of
communicating actors on one thread) stay on their threads, while coordinators that are only briefly active (scatter-gather, sagas, fan-out) run in the pool and do not queue up behind the steady load.

```java
// placement by marker
class Coordinator extends Actor implements PooledActor { ... }

// or by rule, without changing the actor code
ActorSystem system = HybridActorRuntime.create(HybridActorSystemConfig.builder()
    .pooledPlacement(actor -> actor.getClass().getName().startsWith("com.example.coordination."))
    .pooledParallelism(8)      // pooled threads, default max(2, thread-bound/2)
    .parallelism(16)           // thread-bound threads, default half of the cores
    .build());
```

Keep thread-bound + pooled threads at or below the number of logical cores: busy thread-bound threads would
otherwise delay the pooled ones. Without placement rule (no `PooledActor`), the hybrid runtime behaves like the
default runtime. Call the hybrid setters of the builder before the inherited ones.

### Learn more
- [Documentation](https://actor4j.io/documentation/)
- [Specification](https://github.com/relvaner/actor4j-spec)
- [Publications](https://actor4j.io/publications/)

Last updated: November 11, 2026
