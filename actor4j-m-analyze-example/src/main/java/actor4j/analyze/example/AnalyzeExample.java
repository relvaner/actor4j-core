/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyze.example;

import java.util.UUID;

import actor4j.analyze.DefaultActorAnalyzerThread;
import actor4j.core.Actor;
import actor4j.core.ActorGroup;
import actor4j.core.ActorMessage;
import actor4j.core.ActorSystem;
import actor4j.core.HubPattern;

public class AnalyzeExample {
	public AnalyzeExample() {
		ActorSystem system = new ActorSystem();
		final ActorSystem global = system;
		
		int size = 2;
		ActorGroup group = new ActorGroup();
		for (int i=0; i<size; i++) {
			UUID id = system.addActor(new Actor("group-"+i) {
				protected boolean first = true;
				protected UUID last;
				@Override
				protected void receive(ActorMessage<?> message) {
					if (first) {
						UUID next = id;
						for (int i=0; i<3; i++) {
							UUID current = addChild(new Sender("child-"+i, next));
							next = current;
						}
						last = next;
						first = false;
					}
					if (message.tag==1)
						send(new ActorMessage<Object>(null, 0, getId(), last));
				}
			});
			group.add(id);
		}
		UUID id = system.addActor(new Actor("group-"+size) {
			protected HubPattern hub = new HubPattern(global);
			protected boolean first = true;
			@Override
			protected void receive(ActorMessage<?> message) {
				if (first) {
					for (int i=0; i<4; i++) {
						UUID childId = addChild(new Actor("child-"+i){
							@Override
							protected void receive(ActorMessage<?> message) {
							}
						});
						hub.add(childId);
					}
					first = false;
				}
				hub.broadcast(new ActorMessage<Object>(null, 0, getId(), null));
			}
		});
		group.add(id);
		
		UUID ping = system.addActor(new Actor("ping") {
			protected boolean first = true;
			protected UUID pong;
			@Override
			protected void receive(ActorMessage<?> message) {
				if (first) {
					pong = addChild(new Actor("pong") {
						@Override
						protected void receive(ActorMessage<?> message) {
							UUID buffer = message.source;
							message.source = message.dest;
							message.dest = buffer;
							send(message);
						}
					});
					first = false;
				}
				if (message.tag==1)
					send(new ActorMessage<Object>(null, 0, getId(), pong));
			}
		});
		group.add(ping);

		system
			.analyze(new DefaultActorAnalyzerThread(2000, true))
			.start();
		
		system.timer()
			.schedule(new ActorMessage<Object>(null, 1, system.SYSTEM_ID, null), group, 0, 500);
		try {
			Thread.sleep(240000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown();
	}

	public static void main(String[] args) {
		new AnalyzeExample();
	}
}
