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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import io.actor4j.core.ActorPodService;
import io.actor4j.core.ActorService;
import io.actor4j.core.ActorSystemFactory;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.pods.PodReplicationController;
import io.actor4j.core.utils.ActorFactory;

public interface InternalActorSystem extends ActorService, ActorPodService {
	public <T> Queue<T> createLockFreeLinkedQueue();
	public ActorSystemFactory factory();

	public boolean isEmpty();

	public ActorId ZERO_ID();
	public ActorId ALIAS_ID();
	
	public ActorId UNKNOWN_ID();
	public ActorId PSEUDO_ID();

	public PodReplicationController getPodReplicationController();
	public PodReplicationControllerRunnableFactory getPodReplicationControllerRunnableFactory();
	public WatchdogRunnableFactory getWatchdogRunnableFactory();
	
	public PseudoActorCellFactory getPseudoActorCellFactory();

	public Map<UUID, ActorId> getExposedCells();
	public Map<ActorId, InternalActorCell> getPseudoCells();
	public Map<ActorId, Boolean> getResourceCells();
	public Map<ActorId, Boolean> getPodCells();
	
	public Map<String, Queue<ActorId>> getPodDomains();
	public Map<String, Queue<ActorId>> getAliases();
	
	public AtomicBoolean getMessagingEnabled();
	public ActorMessageDispatcher getMessageDispatcher();
	public Queue<ActorMessage<?>> getBufferQueue();
	
	public ActorExecutorService getExecutorService();
	public ActorStrategyOnFailure getStrategyOnFailure();
	
	public InternalActorCell generateCell(Actor actor);
	public InternalActorCell generateCell(Class<? extends Actor> clazz);
	public ActorId internal_addCell(InternalActorCell cell);
	public ActorId pseudo_addCell(InternalActorCell cell);
	
	public boolean internal_iterateCell(InternalActorCell root, Function<InternalActorCell, Boolean> action);
	
	public ActorId addSystemActor(ActorFactory factory);
	public List<ActorId> addSystemActor(ActorFactory factory, int instances);
	
	public void removeActor(ActorId id);
	
	public void sendAsDirective(ActorMessage<?> message);
	
	public boolean primaryPodDeployed(String domain);
}
