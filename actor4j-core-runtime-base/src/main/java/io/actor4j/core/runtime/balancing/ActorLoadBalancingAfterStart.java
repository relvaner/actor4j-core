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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorDistributedGroupMember;
import io.actor4j.core.actors.ActorGroupMember;
import io.actor4j.core.actors.ActorIgnoreDistributedGroupMember;
import io.actor4j.core.runtime.InternalActorCell;

public class ActorLoadBalancingAfterStart {
	protected int groupsDistributedMapIndex;
	
	protected final Lock lock_groupsMap;
	protected final Lock lock_groupsDistributedMap;
	
	public ActorLoadBalancingAfterStart() {
		super();
		
		groupsDistributedMapIndex = 0;
		
		lock_groupsMap = new ReentrantLock();
		lock_groupsDistributedMap = new ReentrantLock();
	}
	
//	public void reset() {
//		groupsDistributedMapIndex = 0;
//	}
	
	public void registerCell(List<Long> executionUnitList, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, InternalActorCell cell) {
		final Actor actor = cell.getActor();
		if (actor instanceof ActorDistributedGroupMember && !(actor instanceof ActorIgnoreDistributedGroupMember)) {
			UUID distributedGroupId = ((ActorDistributedGroupMember)actor).getDistributedGroupId();
			lock_groupsDistributedMap.lock();
			try {
				Integer threadIndex = groupsDistributedMap.get(distributedGroupId);
				Long threadId = null;
				if (threadIndex==null) {
					threadId = executionUnitList.get(groupsDistributedMapIndex);
					groupsDistributedMap.put(distributedGroupId,groupsDistributedMapIndex);
				}
				else {
					threadIndex++;
					if (threadIndex==executionUnitList.size())
						threadIndex = 0;
					groupsDistributedMap.put(distributedGroupId, threadIndex);
					threadId = executionUnitList.get(threadIndex);
				}
				cell.setThreadId(threadId);
				
				groupsDistributedMapIndex = groupsDistributedMapIndex==executionUnitList.size()-1 ? 0 : groupsDistributedMapIndex+1;
				
				if (actor instanceof ActorGroupMember) {
					UUID groupId = ((ActorGroupMember)actor).getGroupId();
					lock_groupsMap.lock();
					try {
						if (groupsMap.get(groupId)==null)
							groupsMap.put(groupId, threadId);
						else
							systemLogger().log(ERROR, String.format("[LOAD BALANCING] actor (%s) must be first initial group member", actorLabel(cell.getActor())));
					}
					finally {
						lock_groupsMap.unlock();
					}
				}
			}
			finally {
				lock_groupsDistributedMap.unlock();
			}
		}
		else if (actor instanceof ActorGroupMember) {
			UUID groupId = ((ActorGroupMember)actor).getGroupId();
			Long threadId = null;
			lock_groupsMap.lock();
			try {
				threadId = groupsMap.get(groupId);
				if (threadId==null) {
					threadId = executionUnitList.get(ThreadLocalRandom.current().nextInt(executionUnitList.size()));
					groupsMap.put(groupId, threadId);
				}
			}
			finally {
				lock_groupsMap.unlock();
			}
			
			cell.setThreadId(threadId);
		}
		else {
			Long threadId = executionUnitList.get(ThreadLocalRandom.current().nextInt(executionUnitList.size()));
			cell.setThreadId(threadId);
		}
	}
	
	public void unregisterCell(List<Long> processList, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, InternalActorCell cell) {
		/*
		 * eventually remove the group (when no more group members are available), for ActorGroupMember, ActorDistributedGroupMember
		 */
		cell.setThreadId(-1);
	}
}
