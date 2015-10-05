package actor4j.core;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActorBalancingOnRuntime {
	protected ActorSystem system;
	
	protected Queue<Long> balancedThreadsQueue;
	
	public ActorBalancingOnRuntime(ActorSystem system) {
		super();
		
		this.system = system;
		
		balancedThreadsQueue = new ConcurrentLinkedQueue<>();
	}
	
	public void registerActor(Map<UUID, Long> actorsMap, Map<Long, ActorThread> threadsMap, Map<UUID, Long> groupsMap, Actor actor) {
		if (actor instanceof ActorGroupMember) {
			Long threadId = groupsMap.get(((ActorGroupMember)actor).getGroupId());
			if (threadId==null) 
				groupsMap.put(((ActorGroupMember)actor).getGroupId(), pollThreadId(threadsMap));
			else
				actorsMap.put(actor.getId(), threadId);
		}
		else
			actorsMap.put(actor.getId(), pollThreadId(threadsMap));
	}
	
	public void unregisterActor(Map<UUID, Long> actorsMap, Map<Long, ActorThread> threadsMap, Map<UUID, Long> groupsMap, Actor actor) {
		if (actor instanceof ActorGroupMember) {
			// TODO: ???
		}
		
		actorsMap.remove(actor.getId());
	}
	
	public Long pollThreadId(Map<Long, ActorThread> threadsMap) {
		Long result = balancedThreadsQueue.poll();
		if (result==null) {
			Iterator<Long> iterator = threadsMap.keySet().iterator();
			while (iterator.hasNext())
				balancedThreadsQueue.offer(iterator.next());
			result = balancedThreadsQueue.poll();
		}
			
		return result;
	}
}
