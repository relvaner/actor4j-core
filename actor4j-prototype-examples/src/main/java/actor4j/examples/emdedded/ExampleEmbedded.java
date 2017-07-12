/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.examples.emdedded;

import static actor4j.core.utils.ActorLogger.logger;

import java.util.UUID;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.actors.EmbeddedActor;
import actor4j.core.messages.ActorMessage;

public class ExampleEmbedded {
	protected final int SWAP=22;
	
	public ExampleEmbedded() {
		ActorSystem system = new ActorSystem("ExampleEmbedded");
		
		UUID host = system.addActor(() -> new Actor("host") {
			protected EmbeddedActor client;
			@Override
			public void preStart() {
				client = new EmbeddedActor("host:client", this) {
					@Override
					public boolean receive(ActorMessage<?> message) {
						boolean result = false;
						
						if (message.tag == SWAP) {
							become(msg -> {
								logger().info(String.format(
										"Received String message: %s", msg.valueAsString()));
								unbecome(); 
								return true;
							}, false);
							result = true;
						}
						
						return result;
					}
				};
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (!client.embedded(message))
					unhandled(message);
			}
		});
		
		system.send(new ActorMessage<Object>(null, SWAP, system.SYSTEM_ID, host));
		system.send(new ActorMessage<Object>("Hello World!", 0, system.SYSTEM_ID, host));
		system.send(new ActorMessage<Object>(null, SWAP, system.SYSTEM_ID, host));
		system.send(new ActorMessage<Object>("Hello World Again!", 0, system.SYSTEM_ID, host));
		system.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	public static void main(String[] args) {
		new ExampleEmbedded();
	}
}
