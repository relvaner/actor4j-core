/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.examples.publish.subscribe;

import static actor4j.core.utils.ActorLogger.logger;

import java.util.Random;
import java.util.UUID;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.publish.subscribe.BrokerActor;
import actor4j.core.publish.subscribe.Publish;
import actor4j.core.publish.subscribe.Subscribe;

public class ExamplePublishSubscribe {
	public ExamplePublishSubscribe() {
		ActorSystem system = new ActorSystem("ExamplePublishSubscribe");
		
		UUID broker = system.addActor(() -> new BrokerActor());
		
		UUID subscriberA = system.addActor(() -> new Actor("subscriberA") {
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("Message received (%s): %s", name, ((Publish<?>)message.value).value));
			}
		});
		UUID subscriberB = system.addActor(() -> new Actor("subscriberB") {
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("Message received (%s): %s", name, ((Publish<?>)message.value).value));
			}
		});
		
		system.send(new ActorMessage<Subscribe>(new Subscribe("MyTopic"), 0, subscriberA, broker));
		system.send(new ActorMessage<Subscribe>(new Subscribe("MyTopic"), 0, subscriberB, broker));
		
		system.addActor(() -> new Actor("publisher") {
			protected Random random;
			@Override
			public void preStart() {
				random = new Random();
				send(new ActorMessage<Publish<String>>(new Publish<String>("MyTopic", String.valueOf(random.nextInt(512))), BrokerActor.GET_TOPIC_ACTOR, self(), broker));
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag==BrokerActor.GET_TOPIC_ACTOR) { 
					system.timer().schedule(() -> new ActorMessage<Publish<String>>(new Publish<String>("MyTopic", String.valueOf(random.nextInt(512))), 0, null, null), message.valueAsUUID(), 0, 100);
				}
			}
		});
		
		/*
		Random random = new Random();
		system.timer().schedule(() -> new ActorMessage<Publish<String>>(new Publish<String>("MyTopic", String.valueOf(random.nextInt(512))), 0, null, null), broker, 0, 100);
		*/
		system.start();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	public static void main(String[] args) {
		new ExamplePublishSubscribe();
	}
}
