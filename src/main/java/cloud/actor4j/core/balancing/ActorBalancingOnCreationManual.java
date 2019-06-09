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
import java.util.UUID;

import cloud.actor4j.core.ActorThread;
import cloud.actor4j.core.utils.ActorGroup;

// currently not used
public abstract class ActorBalancingOnCreationManual {
	public abstract void balance(Map<UUID, Long> cellsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap);
	
	public void balanceActor(Map<UUID, Long> cellsMap, Long threadId, UUID actorID) {
		cellsMap.put(actorID, threadId);
	}
	
	public void balanceActors(Map<UUID, Long> cellsMap, List<ActorThread> actorThreads, List<UUID> actorIDs) {
		int i=0;
		for (UUID id: actorIDs) {
			balanceActor(cellsMap, actorThreads.get(i).getId(), id);
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
	}
	
	public void balanceGroup(Map<UUID, Long> cellsMap, Map<UUID, Long> groupsMap, Long threadId, ActorGroup group) {
		Long foundThreadId = groupsMap.get(group.getId());
		if (foundThreadId==null)
			groupsMap.put(group.getId(), threadId);
		else
			threadId = foundThreadId;
		
		for (UUID id : group)
			cellsMap.put(id, threadId);
	}
	
	public void balanceGroups(Map<UUID, Long> cellsMap, List<ActorThread> actorThreads, Map<UUID, Long> groupsMap, List<ActorGroup> groups) {
		int i=0;
		for (ActorGroup group : groups) {
			balanceGroup(cellsMap, groupsMap, actorThreads.get(i).getId(), group);
			i++;
			if (i==actorThreads.size())
				i = 0;
		}
	}
}
