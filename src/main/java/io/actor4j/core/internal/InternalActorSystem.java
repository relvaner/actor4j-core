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
package io.actor4j.core.internal;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import io.actor4j.core.ActorPodService;
import io.actor4j.core.ActorService;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.internal.di.DIContainer;
import io.actor4j.core.internal.pods.PodReplicationController;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;

public interface InternalActorSystem extends ActorService, ActorPodService {
	public UUID UNKNOWN_ID();
	public UUID PSEUDO_ID();
	
	public DIContainer<UUID> getContainer();
	public PodReplicationController getPodReplicationController();
	public PodReplicationControllerRunnableFactory getPodReplicationControllerRunnableFactory();
	public WatchdogRunnableFactory getWatchdogRunnableFactory();
	
	public Map<UUID, InternalActorCell> getCells();
	public Map<UUID, InternalActorCell> getPseudoCells();
	public Map<UUID, Boolean> getResourceCells();
	public Map<UUID, Boolean> getPodCells();
	
	public Map<String, Queue<UUID>> getPodDomains();
	public Map<String, Queue<UUID>> getAliases();
	public Map<UUID, UUID> getRedirector();
	
	public AtomicBoolean getMessagingEnabled();
	public ActorMessageDispatcher getMessageDispatcher();
	public Queue<ActorMessage<?>> getBufferQueue();
	
	public ActorExecuterService getExecuterService(); // TODO: make abstract/interface
	public ActorThreadFactory getActorThreadFactory();
	public ActorStrategyOnFailure getActorStrategyOnFailure();
	
	public InternalActorCell generateCell(Actor actor);
	public InternalActorCell generateCell(Class<? extends Actor> clazz);
	public UUID internal_addCell(InternalActorCell cell);
	public UUID pseudo_addCell(InternalActorCell cell);
	
	public UUID addSystemActor(ActorFactory factory);
	public List<UUID> addSystemActor(ActorFactory factory, int instances);
	
	public void removeActor(UUID id);
	
	public void sendAsDirective(ActorMessage<?> message);
}
