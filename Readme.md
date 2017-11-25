## Actor4j Core ##

This is the repository for Actor4j core.

>Aim of this project was to enhance the performance in message passing. As a reference implementation `Akka` was used. Results of the research shown that intra-thread-communication is much better than inter-thread-communication. You can group actors, so they are bound to the same thread, for instance. Message queues of the actors are outsourced to the thread. The four principles of reactive manifesto and the four semantic properties of actor systems have been applied.

For further information on `Actor4j`, see the following more complete [documentation](https://github.com/relvaner/actor4j-doc).

## Dependencies ##

Following dependency from this site is involved:
```xml
		<dependency>
			<groupId>safety4j</groupId>
			<artifactId>safety4j</artifactId>
			<version>0.1.1</version>
		</dependency>
```

## Simple Example ##
```java
// Initialize the actor system
ActorSystem system = new ActorSystem("Example");
		
// Creation of actor pong
UUID pong = system.addActor(() -> new Actor() {
	@Override
	public void receive(ActorMessage<?> message) {
		// Receives message from ping
		System.out.println(message.valueAsString());
		// Sends message "pong" to ping
		tell("pong", 0, message.source);
	}
});
// Creation of actor ping
UUID ping = system.addActor(() -> new Actor() {
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null)
			// Receives message from pong
			System.out.println(message.valueAsString());
		// Sends message "ping" to pong
		tell("ping", 0, pong);
	}
});
		
// Starts the actor system
system.start();
		
// Sends a message to ping to start the ping-pong process
system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, ping));
		
// Lifetime for the ping-pong process
try {
	Thread.sleep(2000);
} catch (InterruptedException e) {
	e.printStackTrace();
}
// Wait until all actors are shutdown
system.shutdownWithActors(true);
```

Page last updated 09/18/2017

[link](https://github.com/relvaner/actor4j-doc/blob/master/Readme.md)

