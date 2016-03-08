/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.ArrayList;
import java.util.List;

public class DefaultActorSystemImpl extends ActorSystemImpl {
	public DefaultActorSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}
	
	public DefaultActorSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
		
		messageDispatcher = new DefaultActorMessageDispatcher(this);
		actorThreadClass  = DefaultActorThread.class;
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
