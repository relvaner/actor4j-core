/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.balancing;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorThread;
import actor4j.core.actors.ActorGroupMember;

public class ActorBalancingOnCreation {
	public void balance(Map<UUID, Long> actorsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap, Map<UUID, Actor> actors) {
		List<UUID> buffer = new LinkedList<>();
		for (UUID id : actors.keySet())
			buffer.add(id);
		
		int i=0;
		for (Actor actor : actors.values()) {
			if (actor instanceof ActorGroupMember) {
				Long id = groupsMap.get(((ActorGroupMember)actor).getGroupId());
				if (id==null) {
					id = actorThreads.get(i).getId();
					groupsMap.put(((ActorGroupMember)actor).getGroupId(), id);
					i++;
					if (i==actorThreads.size())
						i = 0;
				}
				if (buffer.remove(actor.id))
					actorsMap.put(actor.id, id);
			}
		}	
						
		i=0;
		for (UUID id : buffer) {
			actorsMap.put(id, actorThreads.get(i).getId());
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
			
		/*
		int i=0;
		for (UUID id : system.actors.keySet()) {
			actorsMap.put(id, actorThreads.get(i).getId());
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
		*/
	}
}
