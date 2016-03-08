/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyzer.example;

import java.util.UUID;

import actor4j.analyzer.ActorAnalyzer;
import actor4j.analyzer.DefaultActorAnalyzerThread;
import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.core.utils.ActorGroup;
import actor4j.core.utils.HubPattern;

public class AnalyzerExample {
	public AnalyzerExample() {
		ActorSystem system = new ActorAnalyzer(new DefaultActorAnalyzerThread(2000, true));

		final int size = 2;
		ActorGroup group = new ActorGroup();
		for (int i=0; i<size; i++) {
			final int f_i = i;
			UUID id = system.addActor(new ActorFactory() {
				@Override
				public Actor create() {
					return new Actor("group-"+f_i) {
						protected boolean first = true;
						protected UUID last;
						@Override
						public void receive(ActorMessage<?> message) {
							if (first) {
								UUID next = self();
								for (int i=0; i<3; i++) {
									final int f_i = i;
									final UUID f_next = next;
									UUID current = addChild(Sender.class, "child-"+f_i, f_next);
									next = current;
								}
								last = next;
								first = false;
							}
							if (message.tag==1)
								send(new ActorMessage<Object>(null, 0, self(), last));
						}
					};
				}
			});
			group.add(id);
		}
		UUID id = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Actor("group-"+size) {
					protected HubPattern hub = new HubPattern(this);
					protected boolean first = true;
					@Override
					public void receive(ActorMessage<?> message) {
						if (first) {
							for (int i=0; i<4; i++) {
								final int f_i = i;
								UUID childId = addChild(new ActorFactory() {
									@Override
									public Actor create() {
										return new Actor("child-"+f_i){
											@Override
											public void receive(ActorMessage<?> message) {
											}
										};
									}
								});
								hub.add(childId);
							}
							first = false;
						}
						hub.broadcast(new ActorMessage<Object>(null, 0, self(), null));
					}
				};
			}
		});
		group.add(id);
		
		UUID ping = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Actor("ping") {
					protected boolean first = true;
					protected UUID pong;
					@Override
					public void receive(ActorMessage<?> message) {
						if (first) {
							pong = addChild(new ActorFactory() {
								@Override
								public Actor create() {
									return new Actor("pong") {
										@Override
										public void receive(ActorMessage<?> message) {
											UUID buffer = message.source;
											message.source = message.dest;
											message.dest = buffer;
											send(message);
										}
									};
								}
							});
							first = false;
						}
						if (message.tag==1)
							send(new ActorMessage<Object>(null, 0, self(), pong));
					}
				};
			}
		});
		group.add(ping);

		system
			.start();
		
		system.timer()
			.schedule(new ActorMessage<Object>(null, 1, system.SYSTEM_ID, null), group, 0, 500)
			.scheduleOnce(new ActorMessage<Object>(null, Actor.RESTART, system.SYSTEM_ID, null), ping, 5000)
			.scheduleOnce(new ActorMessage<Object>(null, Actor.STOP, system.SYSTEM_ID, null), id, 15000)
			.scheduleOnce(new ActorMessage<Object>(null, Actor.STOP, system.SYSTEM_ID, null), system.USER_ID, 25000);
		
		try {
			Thread.sleep(240000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}

	public static void main(String[] args) {
		new AnalyzerExample();
	}
}
