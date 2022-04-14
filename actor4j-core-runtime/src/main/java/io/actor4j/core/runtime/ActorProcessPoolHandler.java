/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.annotations.concurrent.Readonly;
import io.actor4j.core.runtime.balancing.ActorLoadBalancingAfterStart;
import io.actor4j.core.runtime.balancing.ActorLoadBalancingBeforeStart;
import io.actor4j.core.runtime.persistence.ActorPersistenceServiceImpl;

public class ActorProcessPoolHandler<P extends ActorProcess> {
	protected final InternalActorSystem system;
	
	protected final Map<UUID, Long> cellsMap;  // ActorCellID -> ThreadID
	@Readonly
	protected final Map<Long, P> threadsMap;
	@Readonly
	protected final List<Long> threadsList;
	@Readonly
	protected final Map<Long, String> persistenceMap;
	
	protected final Map<UUID, Long> groupsMap; // GroupID -> ThreadID
	protected final Map<UUID, Integer> groupsDistributedMap; // GroupID -> ThreadIndex
	
	protected final ActorLoadBalancingBeforeStart actorLoadBalancingBeforeStart;
	protected final ActorLoadBalancingAfterStart actorLoadBalancingAfterStart;
	
	public ActorProcessPoolHandler(InternalActorSystem system) {
		super();
		
		this.system = system;
		
		cellsMap = new ConcurrentHashMap<>();
		threadsMap = new HashMap<>();
		threadsList = new ArrayList<>();
		persistenceMap = new HashMap<>();
		
		groupsMap = new ConcurrentHashMap<>();
		groupsDistributedMap = new ConcurrentHashMap<>();
		
		actorLoadBalancingBeforeStart = new ActorLoadBalancingBeforeStart();
		actorLoadBalancingAfterStart = new ActorLoadBalancingAfterStart();
	}
	
	public Map<UUID, Long> getCellsMap() {
		return cellsMap;
	}

	public Map<Long, P> getThreadsMap() {
		return threadsMap;
	}
	
	public List<Long> getThreadsList() {
		return threadsList;
	}

	public void beforeStart(List<P> actorThreads) {
		int i=0;
		for(P p : actorThreads) {
			threadsMap.put(p.getId(), p);
			threadsList.add(p.getId());
			persistenceMap.put(p.getId(), ActorPersistenceServiceImpl.getAlias(i));
			i++;
		}
		
		actorLoadBalancingBeforeStart.registerCells(cellsMap, threadsList, groupsMap, groupsDistributedMap, system.getCells());
	}
	
	public void postPersistence(ActorMessage<?> message) {
		Long id_source = cellsMap.get(message.source()); // message.source matches original actor
		UUID dest = system.getExecuterService().getPersistenceService().getService().getActorFromAlias(persistenceMap.get(id_source));
		system.getExecuterService().getPersistenceService().getService().send(message.copy(dest));
	}
	
	public void registerCell(InternalActorCell cell) {
		actorLoadBalancingAfterStart.registerCell(cellsMap, threadsList, groupsMap, groupsDistributedMap, cell);
	}
	
	public void unregisterCell(InternalActorCell cell) {
		actorLoadBalancingAfterStart.unregisterCell(cellsMap, threadsList, groupsMap, groupsDistributedMap, cell);
	}
	
	public boolean isRegisteredCell(InternalActorCell cell) {
		return cellsMap.containsKey(cell.getId());
	}
}
