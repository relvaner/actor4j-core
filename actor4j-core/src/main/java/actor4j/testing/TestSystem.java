/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.testing;

import java.util.UUID;
import java.util.concurrent.Future;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.actors.PseudoActor;
import actor4j.core.messages.ActorMessage;

public class TestSystem extends ActorSystem {
	public TestSystem() {
		super("actor4j-test", TestSystemImpl.class);
		
		((TestSystemImpl)system).pseudoActor = new PseudoActor(this) {
			@Override
			public void receive(ActorMessage<?> message) {
				((TestSystemImpl)system).actualMessage.complete(message);
			}
		};
	}
	
	public ActorCell underlyingCell(UUID id) {
		return ((TestSystemImpl)system).underlyingCell(id);
	}
	
	public Actor underlyingActor(UUID id) {
		return ((TestSystemImpl)system).underlyingActor(id);
	}
	
	public void testActor(UUID id) {
		((TestSystemImpl)system).testActor(id);
	}
	
	public void testAllActors() {
		((TestSystemImpl)system).testAllActors();
	}
	
	public Future<ActorMessage<?>> awaitMessage() {
		return ((TestSystemImpl)system).awaitMessage();
	}
}
