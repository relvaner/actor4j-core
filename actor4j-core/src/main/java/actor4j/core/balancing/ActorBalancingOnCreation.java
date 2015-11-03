/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.balancing;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import actor4j.core.ActorCell;
import actor4j.core.ActorThread;
import actor4j.core.actors.Actor;
import actor4j.core.actors.ActorGroupMember;

public class ActorBalancingOnCreation {
	public void balance(Map<UUID, Long> cellsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap, Map<UUID, ActorCell> cells) {
		List<UUID> buffer = new LinkedList<>();
		for (UUID id : cells.keySet())
			buffer.add(id);
		
		int i=0;
		for (ActorCell cell : cells.values()) {
			Actor actor = cell.getActor();
			if (actor instanceof ActorGroupMember) {
				Long id = groupsMap.get(((ActorGroupMember)actor).getGroupId());
				if (id==null) {
					id = actorThreads.get(i).getId();
					groupsMap.put(((ActorGroupMember)actor).getGroupId(), id);
					i++;
					if (i==actorThreads.size())
						i = 0;
				}
				if (buffer.remove(cell.getId()))
					cellsMap.put(cell.getId(), id);
			}
		}	
						
		i=0;
		for (UUID id : buffer) {
			cellsMap.put(id, actorThreads.get(i).getId());
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
			
		/*
		int i=0;
		for (UUID id : system.cells.keySet()) {
			cellsMap.put(id, actorThreads.get(i).getId());
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
		*/
	}
}
