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
package actor4j.core.balancing;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import actor4j.core.ActorCell;
import actor4j.core.ActorThread;
import actor4j.core.actors.Actor;
import actor4j.core.actors.ActorGroupMember;

public class ActorBalancingOnRuntime {
	protected Queue<Long> balancedThreadsQueue;
	
	public ActorBalancingOnRuntime() {
		super();
		
		balancedThreadsQueue = new ConcurrentLinkedQueue<>();
	}
	
	public void registerCell(Map<UUID, Long> cellsMap, Map<Long, ActorThread> threadsMap, Map<UUID, Long> groupsMap, ActorCell cell) {
		Actor actor = cell.getActor();
		if (actor instanceof ActorGroupMember) {
			Long threadId = groupsMap.get(((ActorGroupMember)actor).getGroupId());
			if (threadId==null) {
				threadId = pollThreadId(threadsMap);
				groupsMap.put(((ActorGroupMember)actor).getGroupId(), threadId);
			}
			
			cellsMap.put(cell.getId(), threadId);
		}
		else
			cellsMap.put(cell.getId(), pollThreadId(threadsMap));
	}
	
	public void unregisterCell(Map<UUID, Long> cellsMap, Map<Long, ActorThread> threadsMap, Map<UUID, Long> groupsMap, ActorCell cell) {
		if (cell.getActor() instanceof ActorGroupMember) {
			// TODO: ???
		}
		
		cellsMap.remove(cell.getId());
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
