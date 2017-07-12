/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.examples.pseudo;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import actor4j.core.ActorSystem;
import actor4j.core.ActorTimer;
import actor4j.core.actors.Actor;
import actor4j.core.actors.PseudoActor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;

public class ExamplePseudo {
	public ExamplePseudo() {
		ActorSystem system = new ActorSystem("ExamplePseudo");
		
		PseudoActor main = new PseudoActor(system, false) {
			@Override
			public void receive(ActorMessage<?> message) {
			}
		};
		
		UUID numberGenerator = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Actor("numberGenerator") {
					protected ActorTimer timer;
					
					@Override
					public void preStart() {
						Random random = new Random();
						timer = system.timer()
							.schedule(() -> new ActorMessage<Integer>(random.nextInt(512), 0, self(), null), main.getId(), 0, 100);
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
						System.out.printf("numberGenerator received a message.tag (%d) from main%n", message.tag);
					}
					
					@Override
					public void postStop() {
						timer.cancel();
					}
				};
			}
		});
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			protected int i;
			@Override
			public void run() {
				main.runWithRx()
					.take(2)
					.map(msg -> "-> main received a message.value ("+msg.valueAsInt()+") from numberGenerator")
					.subscribe(System.out::println);
					
				main.send(new ActorMessage<>(null, i++, main.getId(), numberGenerator));
			}
		}, 0, 1000);
		
		system.start();
		
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.cancel();
		system.shutdownWithActors(true);
	}
	
	public static void main(String[] args) {
		new ExamplePseudo();
	}
}
