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
package io.actor4j.core.runtime.balancing;

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorDistributedGroupMember;
import io.actor4j.core.actors.ActorGroupMember;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.mutable.MutableObject;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;

public class ActorLoadBalancingBeforeStart {
	public void registerCells(List<Long> executionUnitList, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, InternalActorSystem system) {
		List<ActorId> buffer = new LinkedList<>();
		Function<InternalActorCell, Boolean> registerCells = cell -> {
			if (!(cell.getActor() instanceof ResourceActor))
				buffer.add(cell.getId());
			
			return false;
		};
		system.internal_iterateCell((InternalActorCell)system.SYSTEM_ID(), registerCells);
		system.internal_iterateCell((InternalActorCell)system.USER_ID(), registerCells);
		
		final MutableObject<Integer> i = new MutableObject<>(0);
		final MutableObject<Integer> j = new MutableObject<>(0);
		Function<InternalActorCell, Boolean> registerCells_groups = cell -> {
			Actor actor = cell.getActor();
			
			if (actor instanceof ResourceActor)
				return false;
			
			if (actor instanceof ActorDistributedGroupMember) {
				Integer threadIndex = groupsDistributedMap.get(((ActorDistributedGroupMember)actor).getDistributedGroupId());
				Long threadId = null;
				if (threadIndex==null) {
					threadId = executionUnitList.get(j.getValue());
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getDistributedGroupId(), j.getValue());
				}
				else {
					threadIndex++;
					if (threadIndex==executionUnitList.size())
						threadIndex = 0;
					threadId = executionUnitList.get(threadIndex);
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getDistributedGroupId(), threadIndex);
				}
				if (buffer.remove(cell.getId()))
					cell.setThreadId(threadId);
				j.increment();
				if (j.getValue()==executionUnitList.size())
					j.setValue(0);
				
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
					threadId = executionUnitList.get(i.getValue());
					groupsMap.put(((ActorGroupMember)actor).getGroupId(), threadId);
					i.increment();
					if (i.getValue()==executionUnitList.size())
						i.setValue(0);
				}
				if (buffer.remove(cell.getId()))
					cell.setThreadId(threadId);
			}
			
			return false;
		};
		system.internal_iterateCell((InternalActorCell)system.SYSTEM_ID(), registerCells_groups);
		system.internal_iterateCell((InternalActorCell)system.USER_ID(), registerCells_groups);
					
		i.setValue(0);
		for (ActorId id : buffer) {
			((InternalActorCell)id).setThreadId(executionUnitList.get(i.getValue()));
			i.increment();
			if (i.getValue()==executionUnitList.size())
				i.setValue(0);
		}
			
		/*
		int i=0;
		for (ActorId id : system.cells.keySet()) {
			((InternalActorCell)id).setThreadId(executionUnitList.get(i));
			i++;
			if (i==threadsList.size())
				i = 0;
		}
		*/
	}
}
