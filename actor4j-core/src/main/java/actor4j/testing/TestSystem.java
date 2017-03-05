/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.testing;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.actors.PseudoActor;
import actor4j.core.messages.ActorMessage;

public class TestSystem extends ActorSystem {
	public TestSystem() {
		super("actor4j-test", TestSystemImpl.class);
		
		((TestSystemImpl)system).pseudoActor = new PseudoActor(this, true) {
			@Override
			public void receive(ActorMessage<?> message) {
				((TestSystemImpl)system).actualMessage.complete(message);
			}
		};
		((TestSystemImpl)system).pseudoActorId = ((TestSystemImpl)system).pseudoActor.getId();
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
	
	public ActorMessage<?> awaitMessage(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return ((TestSystemImpl)system).awaitMessage(timeout, unit);
	}
	
	public void assertNoMessages() {
		((TestSystemImpl)system).assertNoMessages();
	}
}
