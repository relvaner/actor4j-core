/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.ArrayList;
import java.util.List;

import actor4j.core.actors.Actor;
import actor4j.core.actors.ResourceActor;

public class DefaultActorSystemImpl extends ActorSystemImpl {
	public DefaultActorSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}
	
	public DefaultActorSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
		
		messageDispatcher = new DefaultActorMessageDispatcher(this);
		actorThreadClass  = DefaultActorThread.class;
	}
	
	@Override
	public ActorCell generateCell(Actor actor) {
		if (actor instanceof ResourceActor)
			return new ResourceActorCell(this, actor);
		else
			return new ActorCell(this, actor);
	}
	
	@Override
	public ActorCell generateCell(Class<? extends Actor> clazz) {
		if (clazz==ResourceActor.class)
			return new ResourceActorCell(this, null);
		else
			return new ActorCell(this, null);
	}
	
	public List<Integer> getWorkerInnerQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : executerService.getActorThreads())
			list.add(((DefaultActorThread)t).getInnerQueue().size());
		return list;
	}
	
	public List<Integer> getWorkerOuterQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : executerService.getActorThreads())
			list.add(((DefaultActorThread)t).getOuterQueue().size());
		return list;
	}
}
