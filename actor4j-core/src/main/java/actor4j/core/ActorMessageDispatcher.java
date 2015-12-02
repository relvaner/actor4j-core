/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import static actor4j.core.utils.ActorUtils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import actor4j.core.messages.ActorMessage;

public abstract class ActorMessageDispatcher {
	protected ActorSystemImpl system;
	
	protected Map<UUID, Long> cellsMap;  // ActorCellID -> ThreadID
	protected Map<Long, ActorThread> threadsMap;
	
	protected Map<UUID, Long> groupsMap; // GroupID -> ThreadID
	
	protected final UUID UUID_ALIAS = UUID_ZERO;
	
	public ActorMessageDispatcher(ActorSystemImpl system) {
		super();
		
		this.system = system;
		
		cellsMap = new ConcurrentHashMap<>();
		threadsMap = new HashMap<>();
		
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
	
	public abstract void postOuter(ActorMessage<?> message);
	
	public abstract void postServer(ActorMessage<?> message);
	
	public abstract void postDirective(ActorMessage<?> message);
	
	public void beforeRun(List<ActorThread> actorThreads) {
		system.actorBalancingOnCreation.balance(cellsMap, actorThreads, groupsMap, system.cells);
		
		for(ActorThread t : actorThreads)
			threadsMap.put(t.getId(), t);
	}
	
	public void registerCell(ActorCell cell) {
		system.actorBalancingOnRuntime.registerCell(cellsMap, threadsMap, groupsMap, cell);
	}
	
	public void unregisterCell(ActorCell cell) {
		system.actorBalancingOnRuntime.unregisterCell(cellsMap, threadsMap, groupsMap, cell);
	}
}
