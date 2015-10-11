/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyze.example;

import java.util.UUID;

import actor4j.analyze.DefaultActorAnalyzerThread;
import actor4j.core.Actor;
import actor4j.core.ActorFactory;
import actor4j.core.ActorGroup;
import actor4j.core.ActorMessage;
import actor4j.core.ActorProtocolTag;
import actor4j.core.ActorSystem;
import actor4j.core.HubPattern;

public class AnalyzeExample {
	public AnalyzeExample() {
		ActorSystem system = new ActorSystem();
		final ActorSystem global = system;
		
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
						protected void receive(ActorMessage<?> message) {
							if (first) {
								UUID next = id;
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
								send(new ActorMessage<Object>(null, 0, getId(), last));
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
					protected HubPattern hub = new HubPattern(global);
					protected boolean first = true;
					@Override
					protected void receive(ActorMessage<?> message) {
						if (first) {
							for (int i=0; i<4; i++) {
								final int f_i = i;
								UUID childId = addChild(new ActorFactory() {
									@Override
									public Actor create() {
										return new Actor("child-"+f_i){
											@Override
											protected void receive(ActorMessage<?> message) {
											}
										};
									}
								});
								hub.add(childId);
							}
							first = false;
						}
						hub.broadcast(new ActorMessage<Object>(null, 0, getId(), null));
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
					protected void receive(ActorMessage<?> message) {
						if (first) {
							pong = addChild(new ActorFactory() {
								@Override
								public Actor create() {
									return new Actor("pong") {
										@Override
										protected void receive(ActorMessage<?> message) {
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
							send(new ActorMessage<Object>(null, 0, getId(), pong));
					}
				};
			}
		});
		group.add(ping);

		system
			.analyze(new DefaultActorAnalyzerThread(2000, true))
			.start();
		
		system.timer()
			.schedule(new ActorMessage<Object>(null, 1, system.SYSTEM_ID, null), group, 0, 500);
		system.timer()
			.scheduleOnce(new ActorMessage<Object>(null, Actor.RESTART, system.SYSTEM_ID, null), ping, 5000);
		system.timer()
			.scheduleOnce(new ActorMessage<Object>(null, Actor.STOP, system.SYSTEM_ID, null), id, 10000);
		
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
