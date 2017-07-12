/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.examples.rxstash;

import java.util.Random;
import java.util.UUID;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.actors.ActorWithRxStash;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.core.utils.ActorMessageMatcher;

public class ExampleRxStash {
	public ExampleRxStash() {
		ActorSystem system = new ActorSystem("ExampleRxStash");
		
		UUID receiver = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new ActorWithRxStash("receiver") {
					protected ActorMessageMatcher matcher;

					@Override
					public void preStart() {
						rxStash = rxStash
							.filter(msg -> msg.valueAsInt() > 50)
							.map(msg -> new ActorMessage<Integer>(msg.valueAsInt(), msg.tag+1976, msg.source, msg.dest));
						
						matcher = new ActorMessageMatcher();
						matcher
							.match(0, msg -> stash.offer(msg))
							.match(msg -> msg.tag>0, msg -> {
								rxStash.subscribe(System.out::println);
							});
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
						if (!matcher.apply(message))
							unhandled(message);
					}
				}; 
			}
		});
		
		UUID sender = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Actor("Sender") {
					protected Random random;
					
					@Override
					public void preStart() {
						random = new Random();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
						send(new ActorMessage<Integer>(random.nextInt(100), random.nextInt(1+1), self(), receiver));
					}
				};
			}
		});
		
		system
			.timer().schedule(new ActorMessage<>(null, 0, null, null), sender, 0, 100);
		
		system.start();
		
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}

	public static void main(String[] args) {
		new ExampleRxStash();
	}
}
