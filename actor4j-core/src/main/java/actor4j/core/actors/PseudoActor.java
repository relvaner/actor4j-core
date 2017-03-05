/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.actors;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

import actor4j.core.ActorSystem;
import actor4j.core.PseudoActorCell;
import actor4j.core.messages.ActorMessage;
import rx.Observable;

public abstract class PseudoActor extends ActorWithRxStash {
	public PseudoActor(ActorSystem system, boolean blocking) {
		this(null, system, blocking);
	}
	
	public PseudoActor(String name, ActorSystem system, boolean blocking) {
		super(name);
		
		PseudoActorCell cell = new PseudoActorCell(system, this, blocking);
		setCell(cell);
		cell.system_addCell(cell);
		/* preStart */
		preStart();
	}

	public boolean run() {
		return ((PseudoActorCell)cell).run();
	}
	
	public boolean runOnce() {
		return ((PseudoActorCell)cell).runOnce();
	}
	
	public Observable<ActorMessage<?>> runWithRx() {
		return ((PseudoActorCell)cell).runWithRx();
	}
	
	public ActorMessage<?> await() {
		return ((PseudoActorCell)cell).await();
	}
	
	public ActorMessage<?> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return ((PseudoActorCell)cell).await(timeout, unit);
	}
	
	public <T> T await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, T> action, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return ((PseudoActorCell)cell).await(predicate, action, timeout, unit);
	}
	
	public void reset() {
		((PseudoActorCell)cell).reset();
	}
}
