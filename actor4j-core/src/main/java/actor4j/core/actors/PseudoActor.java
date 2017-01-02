/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.actors;

import actor4j.core.ActorSystem;
import actor4j.core.PseudoActorCell;
import actor4j.core.messages.ActorMessage;
import rx.Observable;

public abstract class PseudoActor extends ActorWithRxStash {
	public PseudoActor(ActorSystem system) {
		this(null, system);
	}
	
	public PseudoActor(String name, ActorSystem system) {
		super(name);
		
		PseudoActorCell cell = new PseudoActorCell(system, this);
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
	
	public void reset() {
		((PseudoActorCell)cell).reset();
	}
}
