package actor4j.core.mono;

import java.util.ArrayList;
import java.util.List;

import actor4j.core.ActorSystem;
import actor4j.core.ActorSystemImpl;
import actor4j.core.ActorThread;

public class MonoActorSystemImpl extends ActorSystemImpl {
	public MonoActorSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}
	
	public MonoActorSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
		
		messageDispatcher = new MonoActorMessageDispatcher(this);
		actorThreadClass  = MonoActorThread.class;
	}
	
	public List<Integer> getWorkerInnerQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : executerService.getActorThreads())
			list.add(((MonoActorThread)t).getInnerQueue().size());
		return list;
	}
	
	public List<Integer> getWorkerOuterQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : executerService.getActorThreads())
			list.add(((MonoActorThread)t).getOuterQueue().size());
		return list;
	}
}
