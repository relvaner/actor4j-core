## Actor4j an actor implementation ##
Aim of this project was to enhance the performance in message passing. As a reference implementation `Akka` [1] was used. Results of the research shown that intra-thread-communication is much better than inter-thread-communication. You can group actors, so they are bound to the same thread, for instance. Message queues of the actors are outsourced to the thread. The four principles of reactive manifesto [2] and the four semantic properties [3] of actor systems have been applied. The actor system is from extern accessible by the REST-API or by a websocket. Between the nodes are websockets for message transfer established. Time consuming tasks can be outsourced to `ResourceActor's`, which are executed by an extra `ThreadPool`. So the responsiveness of the actor system therfore will not tangented.

>[1] Lightbend (2016). Akka. http://akka.io/  
>[2] Jonas Bonér, Dave Farley, Roland Kuhn, and Martin Thompson (2014). The Reactive Manifesto. http://www.reactivemanifesto.org/  
>[3] Rajesh K. Karmani, Gul Agha (2011). Actors. In Encyclopedia of Parallel Computing, Pages 1–11. Springer. http://osl.cs.illinois.edu/media/papers/karmani-2011-actors.pdf  

## Configuration, starting and stopping the actor system ##
In `actor4j` the following important configuration options are available.
```java
ActorSystem system = new ActorSystem();

system
	.setParallelismMin(1)
	.setParallelismFactor(1);
	.softMode(); // or .hardMode();
```
On the one hand, the number of threads can be set with `setParallelismMin` and the scaling factor with `setParallelismFactor`:

>Number of threads = parallelismMin * parallelismFactor

In addition, it can be determined whether the threads are operating in soft or hard mode if the situation occurs that temporarily no messages are received. The actor system is started with the call:
```java
system.start();
```
The actor system can be terminated, either with controlled shutdown of all actors or not. With a controlled shutdown, a stop directive is sent internally to all actors. By means of parameter transfer, it is possible to determine whether the calling thread waits until the shutdown of the actor system has been completely terminated.
```java
system.shutdown(); // normal shutdown
system.shutdown(true); // shutdown and wait

system.shutdownWithActors(); // shutdown with actors
system.shutdownWithActors(true);
```

`Actor4j` solves a controlled shutdown by sending a termination message to the user actor (father node of all actors, tree structure), which results that the other subordinate actors are terminating in a cascade form. The actors themselves are responsible for an orderly handling of their termination.

## Actors, pattern matching and behaviour ##
There are two possibilities to add actors to the actor system. On one hand, by specifying the class and its constructor (is then instantiated using reflection) or via a factory method. Both variants are passed to a dependency injection container, which can then instantiate the actors accordingly. Actors can be generated outside the actor system, these are automatically subordinated to the user actor (father of all user-generated actors). However, they can also be generated within an actor, but these are then child actors of the corresponding actor. After instantiation, they return a unique `UUID` (unambiguous identification of the actor).
```java
// over reflection
system.addActor(MyActor.class, "MyActor", ...);
// or using a factory method
system.addActor(new ActorFactory() {
	@Override
	public Actor create() {
		 return new MyActor();	
	 }
});

// or in the context of an actor
addChild(MyActor.class, "MyActor", ...);				
// or
UUID myActor = addChild( () -> new MyActor() ); 
```
Actors must derive from the class `Actor` and implement the `receive` method. In the example below, `MyActor` waits for a message that contains a `String` and then outputs it via a logger. Subsequently, the message is sent back to the sender. When a different message is received, a warning (`unhandled (message)`) is output if `debugUnhandled` has been set in the actor system.
```java
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import static actor4j.core.utils.ActorLogger.*;

public class MyActor extends Actor {
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value instanceof String) {
			logger().info(String.format(
				"Received String message: %s", message.valueAsString()));
			send(message, message.dest);
		} 
		else
			unhandled(message);
	}
}		            
```

Page to be updated 11/17/2016