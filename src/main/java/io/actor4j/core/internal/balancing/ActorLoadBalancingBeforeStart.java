/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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
package io.actor4j.core.internal.balancing;

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorDistributedGroupMember;
import io.actor4j.core.actors.ActorGroupMember;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.internal.ActorCell;
import io.actor4j.core.internal.ActorThread;

public class ActorLoadBalancingBeforeStart {
	public void registerCells(Map<UUID, Long> cellsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, Map<UUID, ActorCell> cells) {
		List<UUID> buffer = new LinkedList<>();
		for (ActorCell cell : cells.values()) 
			if (!(cell.getActor() instanceof ResourceActor))
				buffer.add(cell.getId());
		
		int i=0, j=0;
		for (ActorCell cell : cells.values()) {
			Actor actor = cell.getActor();
			
			if (actor instanceof ResourceActor)
				continue;
			
			if (actor instanceof ActorDistributedGroupMember) {
				Integer threadIndex = groupsDistributedMap.get(((ActorDistributedGroupMember)actor).getDistributedGroupId());
				Long threadId = null;
				if (threadIndex==null) {
					threadId = actorThreads.get(j).getId();
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getDistributedGroupId(), j);
				}
				else {
					threadIndex++;
					if (threadIndex==actorThreads.size())
						threadIndex = 0;
					threadId = actorThreads.get(threadIndex).getId();
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getDistributedGroupId(), threadIndex);
				}
				if (buffer.remove(cell.getId()))
					cellsMap.put(cell.getId(), threadId);
				j++;
				if (j==actorThreads.size())
					j = 0;
				
				if (actor instanceof ActorGroupMember) {
					if (groupsMap.get(((ActorGroupMember)actor).getGroupId())==null)
						groupsMap.put(((ActorGroupMember)actor).getGroupId(), threadId);
					else
						systemLogger().log(ERROR, String.format("[LOAD BALANCING] actor (%s) must be first initial group member", actorLabel(cell.getActor())));
				}
			}
			else if (actor instanceof ActorGroupMember) {
				Long threadId = groupsMap.get(((ActorGroupMember)actor).getGroupId());
				if (threadId==null) {
					threadId = actorThreads.get(i).getId();
					groupsMap.put(((ActorGroupMember)actor).getGroupId(), threadId);
					i++;
					if (i==actorThreads.size())
						i = 0;
				}
				if (buffer.remove(cell.getId()))
					cellsMap.put(cell.getId(), threadId);
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
