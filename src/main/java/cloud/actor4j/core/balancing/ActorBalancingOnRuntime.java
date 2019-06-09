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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cloud.actor4j.core.ActorCell;
import cloud.actor4j.core.ActorThread;
import cloud.actor4j.core.actors.Actor;
import cloud.actor4j.core.actors.ActorDistributedGroupMember;
import cloud.actor4j.core.actors.ActorGroupMember;

public class ActorBalancingOnRuntime {
	protected Queue<Long> balancedThreadsQueue;
	protected AtomicInteger pollThreadIndex;
	
	protected Lock lock;
	
	public ActorBalancingOnRuntime() {
		super();
		
		balancedThreadsQueue = new ConcurrentLinkedQueue<>();
		pollThreadIndex = new AtomicInteger(0);
		
		lock = new ReentrantLock();
	}
	
	public void reset() {
		balancedThreadsQueue.clear();
		pollThreadIndex.set(0);
	}
	
	public void registerCell(Map<UUID, Long> cellsMap, List<Long> threadsList, Map<Long, ActorThread> threadsMap, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, ActorCell cell) {
		lock.lock();
		try {
			Actor actor = cell.getActor();
			if (actor instanceof ActorGroupMember) {
				Long threadId = groupsMap.get(((ActorGroupMember)actor).getGroupId());
				if (threadId==null) {
					threadId = pollThreadId(threadsMap);
					groupsMap.put(((ActorGroupMember)actor).getGroupId(), threadId);
				}
				
				cellsMap.put(cell.getId(), threadId);
			}
			else if (actor instanceof ActorDistributedGroupMember) {
				Integer threadIndex = groupsDistributedMap.get(((ActorDistributedGroupMember)actor).getGroupId());
				Long id = null;
				if (threadIndex==null) {
					id = threadsList.get(pollThreadIndex.get());
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getGroupId(), pollThreadIndex.get());
				}
				else {
					threadIndex++;
					if (threadIndex==threadsMap.size())
						threadIndex = 0;
					groupsDistributedMap.put(((ActorDistributedGroupMember)actor).getGroupId(), threadIndex);
					id = threadsList.get(threadIndex);
				}
				cellsMap.put(cell.getId(), id);
				
				pollThreadIndex.updateAndGet((index) -> index==threadsList.size()-1 ? 0 : index+1);
			}
			else
				cellsMap.put(cell.getId(), pollThreadId(threadsMap));
		}
		finally {
			lock.unlock();
		}
	}
	
	public void unregisterCell(Map<UUID, Long> cellsMap, Map<Long, ActorThread> threadsMap, Map<UUID, Long> groupsMap, Map<UUID, Integer> groupsDistributedMap, ActorCell cell) {
		/*
		 * eventually remove the group (when no more group members are avaiable), for ActorGroupMember, ActorDistributedGroupMember
		 */
		
		cellsMap.remove(cell.getId());
	}
	
	public Long pollThreadId(Map<Long, ActorThread> threadsMap) {
		Long result = balancedThreadsQueue.poll();
		if (result==null) {
			for (Long key : threadsMap.keySet())
				balancedThreadsQueue.offer(key);
			result = balancedThreadsQueue.poll();
		}
			
		return result;
	}
}
