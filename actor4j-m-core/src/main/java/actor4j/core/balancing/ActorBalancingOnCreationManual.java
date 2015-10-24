/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.balancing;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorThread;
import actor4j.core.utils.ActorGroup;

public abstract class ActorBalancingOnCreationManual {
	public abstract void balance(Map<UUID, Long> actorsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap);
	
	public void balanceActor(Map<UUID, Long> actorsMap, Long threadId, Actor actor) {
		actorsMap.put(actor.id, threadId);
	}
	
	public void balanceActors(Map<UUID, Long> actorsMap, List<ActorThread> actorThreads, List<Actor> actors) {
		int i=0;
		for (Actor actor : actors) {
			balanceActor(actorsMap, actorThreads.get(i).getId(), actor);
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
	}
	
	public void balanceGroup(Map<UUID, Long> actorsMap, Map<UUID, Long> groupsMap, Long threadId, ActorGroup group) {
		Long foundThreadId = groupsMap.get(group.getId());
		if (foundThreadId==null)
			groupsMap.put(group.getId(), threadId);
		else
			threadId = foundThreadId;
		
		for (UUID id : group)
			actorsMap.put(id, threadId);
	}
	
	public void balanceGroups(Map<UUID, Long> actorsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap, List<ActorGroup> groups) {
		int i=0;
		for (ActorGroup group : groups) {
			balanceGroup(actorsMap, groupsMap, actorThreads.get(i).getId(), group);
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
	}
}
