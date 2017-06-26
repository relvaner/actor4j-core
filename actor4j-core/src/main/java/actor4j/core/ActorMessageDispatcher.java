/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core;

import static actor4j.core.utils.ActorUtils.*;

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
	protected Map<Long, String> persistenceMap;
	
	protected Map<UUID, Long> groupsMap; // GroupID -> ThreadID
	
	protected final UUID UUID_ALIAS = UUID_ZERO;
	
	public ActorMessageDispatcher(ActorSystemImpl system) {
		super();
		
		this.system = system;
		
		cellsMap = new ConcurrentHashMap<>();
		threadsMap = new HashMap<>();
		persistenceMap = new HashMap<>();
		
		groupsMap = new ConcurrentHashMap<>();
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
	
	public abstract void postDirective(ActorMessage<?> message);
	
	public abstract void postPersistence(ActorMessage<?> message);
	
	public void beforeRun(List<ActorThread> actorThreads) {
		system.actorBalancingOnCreation.balance(cellsMap, actorThreads, groupsMap, system.cells);
		
		int i=0;
		for(ActorThread t : actorThreads) {
			threadsMap.put(t.getId(), t);
			persistenceMap.put(t.getId(), ActorPersistenceService.getAlias(i));
			i++;
		}
	}
	
	public void registerCell(ActorCell cell) {
		system.actorBalancingOnRuntime.registerCell(cellsMap, threadsMap, groupsMap, cell);
	}
	
	public void unregisterCell(ActorCell cell) {
		system.actorBalancingOnRuntime.unregisterCell(cellsMap, threadsMap, groupsMap, cell);
	}
}
