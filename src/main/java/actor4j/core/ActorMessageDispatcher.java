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
package actor4j.core;

import static actor4j.core.utils.ActorUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import actor4j.core.messages.ActorMessage;
import actor4j.core.persistence.ActorPersistenceService;

public abstract class ActorMessageDispatcher {
	protected ActorSystemImpl system;
	
	protected Map<UUID, Long> cellsMap;  // ActorCellID -> ThreadID
	protected Map<Long, ActorThread> threadsMap;
	protected List<Long> threadsList;
	protected Map<Long, String> persistenceMap;
	
	protected Map<UUID, Long> groupsMap; // GroupID -> ThreadID
	protected Map<UUID, Integer> groupsDistributedMap;
	
	protected final UUID UUID_ALIAS = UUID_ZERO;
	
	public ActorMessageDispatcher(ActorSystemImpl system) {
		super();
		
		this.system = system;
		
		cellsMap = new ConcurrentHashMap<>();
		threadsMap = new HashMap<>();
		threadsList = new ArrayList<>();
		persistenceMap = new HashMap<>();
		
		groupsMap = new ConcurrentHashMap<>();
		groupsDistributedMap = new ConcurrentHashMap<>();
	}
	
	public void reset() {
		threadsMap.clear();
		threadsList.clear();
		persistenceMap.clear();
		groupsMap.clear();
		groupsDistributedMap.clear();
	}
	
	public Map<UUID, Long> getCellsMap() {
		return cellsMap;
	}

	public Map<Long, ActorThread> getThreadsMap() {
		return threadsMap;
	}

	public void post(ActorMessage<?> message, UUID source) {
		post(message, source, null);
	}
	
	public abstract void post(ActorMessage<?> message, UUID source, String alias);
	
	public abstract void post(ActorMessage<?> message, ActorServiceNode node, String path);
	
	public abstract void postOuter(ActorMessage<?> message);
	
	public abstract void postServer(ActorMessage<?> message);
	
	public abstract void postPriority(ActorMessage<?> message);
	
	public abstract void postDirective(ActorMessage<?> message);
	
	public abstract void postPersistence(ActorMessage<?> message);
	
	public void beforeRun(List<ActorThread> actorThreads) {
		system.actorBalancingOnCreation.balance(cellsMap, actorThreads, groupsMap, groupsDistributedMap, system.cells);
		
		int i=0;
		for(ActorThread t : actorThreads) {
			threadsMap.put(t.getId(), t);
			threadsList.add(t.getId());
			persistenceMap.put(t.getId(), ActorPersistenceService.getAlias(i));
			i++;
		}
	}
	
	public void registerCell(ActorCell cell) {
		system.actorBalancingOnRuntime.registerCell(cellsMap, threadsList, threadsMap, groupsMap, groupsDistributedMap, cell);
	}
	
	public void unregisterCell(ActorCell cell) {
		system.actorBalancingOnRuntime.unregisterCell(cellsMap, threadsMap, groupsMap, groupsDistributedMap, cell);
	}
}
