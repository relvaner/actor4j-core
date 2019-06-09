/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.actor4j.core.balancing;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cloud.actor4j.core.ActorCell;
import cloud.actor4j.core.ActorThread;
import cloud.actor4j.core.actors.Actor;
import cloud.actor4j.core.actors.ActorDistributedGroupMember;
import cloud.actor4j.core.actors.ActorGroupMember;

public class ActorBalancingOnCreation {
	public void balance(Map<UUID, Long> cellsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, Map<UUID, ActorCell> cells) {
		List<UUID> buffer = new LinkedList<>();
		for (UUID id : cells.keySet())
			buffer.add(id);
		
		int i=0, j=0;
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
			else if (actor instanceof ActorDistributedGroupMember) {
				Integer threadIndex = groupsDistributedMap.get(((ActorDistributedGroupMember)actor).getGroupId());
				Long id = null;
				if (threadIndex==null) {
					id = actorThreads.get(j).getId();
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getGroupId(), j);
				}
				else {
					threadIndex++;
					if (threadIndex==actorThreads.size())
						threadIndex = 0;
					id = actorThreads.get(threadIndex).getId();
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getGroupId(), threadIndex);
				}
				if (buffer.remove(cell.getId()))
					cellsMap.put(cell.getId(), id);
				j++;
				if (j==actorThreads.size())
					j = 0;
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
