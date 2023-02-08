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

public class AbstractActorProcessPoolHandler<P extends ActorProcess> implements DefaultActorProcessPoolHandler<P> {
	protected final InternalActorSystem system;
	
	protected final Map<UUID, Long> cellsMap;  // ActorCellID -> ProcessID
	@Readonly
	protected final Map<Long, P> processMap;
	@Readonly
	protected final List<Long> processList;
	@Readonly
	protected final Map<Long, String> persistenceMap;
	
	protected final Map<UUID, Long> groupsMap; // GroupID -> ProcessID
	protected final Map<UUID, Integer> groupsDistributedMap; // GroupID -> ProcessIndex
	
	protected final ActorLoadBalancingBeforeStart actorLoadBalancingBeforeStart;
	protected final ActorLoadBalancingAfterStart actorLoadBalancingAfterStart;
	
	public AbstractActorProcessPoolHandler(InternalActorSystem system) {
		super();
		
		this.system = system;
		
		cellsMap = new ConcurrentHashMap<>();
		processMap = new HashMap<>();
		processList = new ArrayList<>();
		persistenceMap = new HashMap<>();
		
		groupsMap = new ConcurrentHashMap<>();
		groupsDistributedMap = new ConcurrentHashMap<>();
		
		actorLoadBalancingBeforeStart = new ActorLoadBalancingBeforeStart();
		actorLoadBalancingAfterStart = new ActorLoadBalancingAfterStart();
	}

	@Override
	public Map<UUID, Long> getCellsMap() {
		return cellsMap;
	}

	@Override
	public Map<Long, P> getProcessMap() {
		return processMap;
	}

	@Override
	public List<Long> getProcessList() {
		return processList;
	}

	@Override
	public void beforeStart(List<P> actorProcessList) {
		int i=0;
		for(P p : actorProcessList) {
			processMap.put(p.processIdAsLong(), p);
			processList.add(p.processIdAsLong());
			persistenceMap.put(p.processIdAsLong(), ActorPersistenceServiceImpl.getAlias(i));
			i++;
		}
		
		actorLoadBalancingBeforeStart.registerCells(cellsMap, processList, groupsMap, groupsDistributedMap, system.getCells());
	}
	
	@Override
	public void postPersistence(ActorMessage<?> message) {
		Long id_source = cellsMap.get(message.source()); // message.source matches original actor
		UUID dest = system.getExecutorService().getPersistenceService().getService().getActorFromAlias(persistenceMap.get(id_source));
		system.getExecutorService().getPersistenceService().getService().send(message.copy(dest));
	}
	
	@Override
	public void registerCell(InternalActorCell cell) {
		actorLoadBalancingAfterStart.registerCell(cellsMap, processList, groupsMap, groupsDistributedMap, cell);
	}
	
	@Override
	public void unregisterCell(InternalActorCell cell) {
		actorLoadBalancingAfterStart.unregisterCell(cellsMap, processList, groupsMap, groupsDistributedMap, cell);
	}
	
	@Override
	public boolean isRegisteredCell(InternalActorCell cell) {
		return cellsMap.containsKey(cell.getId());
	}
}
