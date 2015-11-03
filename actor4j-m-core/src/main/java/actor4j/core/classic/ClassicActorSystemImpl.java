/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.classic;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;
import actor4j.core.ActorSystemImpl;
import actor4j.core.actors.Actor;

public class ClassicActorSystemImpl extends ActorSystemImpl {
	protected int throughput;
	
	public ClassicActorSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}
	
	public ClassicActorSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
		
		messageDispatcher = new ClassicActorMessageDispatcher(this);
		actorThreadClass  = ClassicActorThread.class;
		
		throughput = 1;
	}

	@Override
	public ActorCell generateDefaultCell(Actor actor) {
		return new ClassicActorCell(this, actor);
	}
	
	public int getThroughput() {
		return throughput;
	}

	public void setThroughput(int throughput) {
		this.throughput = throughput;
	}
}
