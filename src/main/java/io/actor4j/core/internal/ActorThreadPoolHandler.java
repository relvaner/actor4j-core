/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package io.actor4j.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import io.actor4j.core.internal.annotations.concurrent.Readonly;
import io.actor4j.core.internal.balancing.ActorLoadBalancingAfterStart;
import io.actor4j.core.internal.balancing.ActorLoadBalancingBeforeStart;
import io.actor4j.core.internal.persistence.ActorPersistenceService;
import io.actor4j.core.messages.ActorMessage;

public class ActorThreadPoolHandler {
	protected final ActorSystemImpl system;
	
	protected final Map<UUID, Long> cellsMap;  // ActorCellID -> ThreadID
	@Readonly
	protected final Map<Long, ActorThread> threadsMap;
	@Readonly
	protected final List<Long> threadsList;
	@Readonly
	protected final Map<Long, String> persistenceMap;
	
	protected final Map<UUID, Long> groupsMap; // GroupID -> ThreadID
	protected final Map<UUID, Integer> groupsDistributedMap; // GroupID -> ThreadIndex
	
	protected final ActorLoadBalancingBeforeStart actorLoadBalancingBeforeStart;
	protected final ActorLoadBalancingAfterStart actorLoadBalancingAfterStart;
	
	public ActorThreadPoolHandler(ActorSystemImpl system) {
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

	public Map<Long, ActorThread> getThreadsMap() {
		return threadsMap;
	}
	
	public List<Long> getThreadsList() {
		return threadsList;
	}

	public void beforeStart(List<ActorThread> actorThreads) {
		actorLoadBalancingBeforeStart.registerCells(cellsMap, actorThreads, groupsMap, groupsDistributedMap, system.cells);
		
		int i=0;
		for(ActorThread t : actorThreads) {
			threadsMap.put(t.getId(), t);
			threadsList.add(t.getId());
			persistenceMap.put(t.getId(), ActorPersistenceService.getAlias(i));
			i++;
		}
	}
	
	public boolean postInnerOuter(ActorMessage<?> message, UUID source) {
		boolean result = false;
		
		if (system.config.parallelism==1 && system.config.parallelismFactor==1 && Thread.currentThread() instanceof ActorThread) {
			ActorThread t = ((ActorThread)Thread.currentThread());
			t.innerQueue(message.copy());
			t.newMessage();
			result = true;
		}
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(message.dest());
		
			if (id_dest!=null) {
				ActorThread t = threadsMap.get(id_dest);
				
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					t.innerQueue(message.copy());
				else
					t.outerQueue(message.copy());
				
				t.newMessage();
				result = true;
			}
			else
				system.getMessageDispatcher().undelivered(message, source, message.dest());
		}
		
		return result;
	}
	
	public boolean postInnerOuter(ActorMessage<?> message, UUID source, UUID dest) {
		boolean result = false;
		
		if (system.config.parallelism==1 && system.config.parallelismFactor==1 && Thread.currentThread() instanceof ActorThread) {
			ActorThread t = ((ActorThread)Thread.currentThread());
			t.innerQueue(message.copy(dest));
			t.newMessage();
			result = true;
		}
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(dest);
		
			if (id_dest!=null) {
				ActorThread t = threadsMap.get(id_dest);
				
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					t.innerQueue(message.copy(dest));
				else
					t.outerQueue(message.copy(dest));
				
				t.newMessage();
				result = true;
			}	
			else
				system.getMessageDispatcher().undelivered(message, source, dest);
		}
		
		return result;
	}
	
	public boolean postOuter(ActorMessage<?> message) {
		Long id_dest = cellsMap.get(message.dest());
		if (id_dest!=null) {
			ActorThread t = threadsMap.get(id_dest);
			t.outerQueue(message.copy());
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postOuter(ActorMessage<?> message, UUID dest) {
		Long id_dest = cellsMap.get(dest);
		if (id_dest!=null) {
			ActorThread t = threadsMap.get(id_dest);
			t.outerQueue(message.copy(dest));
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postServer(ActorMessage<?> message) {
		Long id_dest = cellsMap.get(message.dest());
		if (id_dest!=null) {
			ActorThread t = threadsMap.get(id_dest);
			t.serverQueue(message.copy());
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postServer(ActorMessage<?> message, UUID dest) {
		Long id_dest = cellsMap.get(dest);
		if (id_dest!=null) {
			ActorThread t = threadsMap.get(id_dest);
			t.serverQueue(message.copy(dest));
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postQueue(ActorMessage<?> message, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		Long id_dest = cellsMap.get(message.dest());
		if (id_dest!=null) {
			ActorThread t = threadsMap.get(id_dest);
			biconsumer.accept(t, message.copy());
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public boolean postQueue(ActorMessage<?> message, UUID dest, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		Long id_dest = cellsMap.get(dest);
		if (id_dest!=null) {
			ActorThread t = threadsMap.get(id_dest);
			biconsumer.accept(t, message.copy(dest));
			t.newMessage();
		}
		
		return id_dest!=null;
	}
	
	public void postPersistence(ActorMessage<?> message) {
		Long id_source = cellsMap.get(message.source()); // message.source matches original actor
		UUID dest = system.executerService.persistenceService.getService().getActorFromAlias(persistenceMap.get(id_source));
		system.executerService.persistenceService.getService().send(message.copy(dest));
	}
	
	public void registerCell(InternalActorCell cell) {
		actorLoadBalancingAfterStart.registerCell(cellsMap, threadsList, threadsMap, groupsMap, groupsDistributedMap, cell);
	}
	
	public void unregisterCell(InternalActorCell cell) {
		actorLoadBalancingAfterStart.unregisterCell(cellsMap, threadsMap, groupsMap, groupsDistributedMap, cell);
	}
	
	public boolean isRegisteredCell(InternalActorCell cell) {
		return cellsMap.containsKey(cell.getId());
	}
}
