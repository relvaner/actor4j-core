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

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.annotations.concurrent.Readonly;
import io.actor4j.core.runtime.balancing.ActorLoadBalancingAfterStart;
import io.actor4j.core.runtime.balancing.ActorLoadBalancingBeforeStart;
import io.actor4j.core.runtime.persistence.ActorPersistenceServiceImpl;

public class AbstractActorExecutionUnitPoolHandler<U extends ActorExecutionUnit> implements DefaultActorExecutionUnitPoolHandler<U> {
	protected final InternalActorSystem system;
	
	protected final Map<ActorId, Long> cellsMap;  // ActorCellID -> ProcessID
	@Readonly
	protected final Map<Long, U> executionUnitMap;
	@Readonly
	protected final List<Long> executionUnitList;
	@Readonly
	protected final Map<Long, String> persistenceMap;
	
	protected final Map<UUID, Long> groupsMap; // GroupID -> ProcessID
	protected final Map<UUID, Integer> groupsDistributedMap; // GroupID -> ProcessIndex
	
	protected final ActorLoadBalancingBeforeStart loadBalancingBeforeStart;
	protected final ActorLoadBalancingAfterStart loadBalancingAfterStart;
	
	public AbstractActorExecutionUnitPoolHandler(InternalActorSystem system) {
		super();
		
		this.system = system;
		
		cellsMap = new ConcurrentHashMap<>();
		executionUnitMap = new HashMap<>();
		executionUnitList = new ArrayList<>();
		persistenceMap = new HashMap<>();
		
		groupsMap = new ConcurrentHashMap<>();
		groupsDistributedMap = new ConcurrentHashMap<>();
		
		loadBalancingBeforeStart = new ActorLoadBalancingBeforeStart();
		loadBalancingAfterStart = new ActorLoadBalancingAfterStart();
	}

	@Override
	public Map<ActorId, Long> getCellsMap() {
		return cellsMap;
	}

	@Override
	public Map<Long, U> getExecutionUnitMap() {
		return executionUnitMap;
	}

	@Override
	public List<Long> getExecutionUnitList() {
		return executionUnitList;
	}

	@Override
	public void beforeStart(List<U> executionUnitListOfU) {
		int i=0;
		for(U u : executionUnitListOfU) {
			executionUnitMap.put(u.executionUnitIdAsLong(), u);
			executionUnitList.add(u.executionUnitIdAsLong());
			persistenceMap.put(u.executionUnitIdAsLong(), ActorPersistenceServiceImpl.getAlias(i));
			i++;
		}
		
		loadBalancingBeforeStart.registerCells(cellsMap, executionUnitList, groupsMap, groupsDistributedMap, system.getCells());
	}
	
	@Override
	public void postPersistence(ActorMessage<?> message) {
		Long id_source = cellsMap.get(message.source()); // message.source matches original actor
		ActorId dest = system.getExecutorService().getPersistenceService().getService().getActorFromAlias(persistenceMap.get(id_source));
		system.getExecutorService().getPersistenceService().getService().send(message.copy(dest));
	}
	
	@Override
	public void registerCell(InternalActorCell cell) {
		loadBalancingAfterStart.registerCell(cellsMap, executionUnitList, groupsMap, groupsDistributedMap, cell);
	}
	
	@Override
	public void unregisterCell(InternalActorCell cell) {
		loadBalancingAfterStart.unregisterCell(cellsMap, executionUnitList, groupsMap, groupsDistributedMap, cell);
	}
	
	@Override
	public boolean isRegisteredCell(InternalActorCell cell) {
		return cellsMap.containsKey(cell.getId());
	}
}
