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
package io.actor4j.core.runtime.balancing;

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorDistributedGroupMember;
import io.actor4j.core.actors.ActorGroupMember;
import io.actor4j.core.actors.ActorIgnoreDistributedGroupMember;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.runtime.InternalActorCell;

public class ActorLoadBalancingAfterStart {
	protected AtomicInteger i;
	protected AtomicInteger j;
	protected AtomicInteger k;
	
	protected Lock lock;
	
	public ActorLoadBalancingAfterStart() {
		super();
		
		i = new AtomicInteger(0);
		j = new AtomicInteger(0);
		k = new AtomicInteger(0);
		
		lock = new ReentrantLock();
	}
	
	public void reset() {
		i.set(0);
		j.set(0);
		k.set(0);
	}
	
	public void registerCell(Map<ActorId, Long> cellsMap, List<Long> executionUnitList, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, InternalActorCell cell) {
		lock.lock();
		try {
			Actor actor = cell.getActor();
			if (actor instanceof ActorDistributedGroupMember && !(actor instanceof ActorIgnoreDistributedGroupMember)) {
				Integer threadIndex = groupsDistributedMap.get(((ActorDistributedGroupMember)actor).getDistributedGroupId());
				Long threadId = null;
				if (threadIndex==null) {
					threadId = executionUnitList.get(j.get());
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getDistributedGroupId(), j.get());
				}
				else {
					threadIndex++;
					if (threadIndex==executionUnitList.size())
						threadIndex = 0;
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getDistributedGroupId(), threadIndex);
					threadId = executionUnitList.get(threadIndex);
				}
				cellsMap.put(cell.getId(), threadId);
				
				j.updateAndGet((index) -> index==executionUnitList.size()-1 ? 0 : index+1);
				
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
					threadId = executionUnitList.get(i.updateAndGet((index) -> index==executionUnitList.size()-1 ? 0 : index+1));
					groupsMap.put(((ActorGroupMember)actor).getGroupId(), threadId);
				}
				
				cellsMap.put(cell.getId(), threadId);
			}
			else {
				Long threadId = executionUnitList.get(k.updateAndGet((index) -> index==executionUnitList.size()-1 ? 0 : index+1));
				cellsMap.put(cell.getId(), threadId);
			}
		}
		finally {
			lock.unlock();
		}
	}
	
	public void unregisterCell(Map<ActorId, Long> cellsMap, List<Long> processList, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, InternalActorCell cell) {
		/*
		 * eventually remove the group (when no more group members are available), for ActorGroupMember, ActorDistributedGroupMember
		 */
		
		cellsMap.remove(cell.getId());
	}
}
