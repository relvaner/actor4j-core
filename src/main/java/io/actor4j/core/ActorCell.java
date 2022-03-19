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
package io.actor4j.core;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.persistence.ActorPersistenceObject;
import io.actor4j.core.utils.ActorFactory;

public interface ActorCell {
	public ActorSystem getSystem();
	
	public UUID getId();
	public UUID getParent();
	public Queue<UUID> getChildren();
	
	public boolean isRoot();
	public boolean isRootInUser();
	
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace);
	public void unbecome();
	public void unbecomeAll();
	
	public void send(ActorMessage<?> message);
	public void send(ActorMessage<?> message, String alias);
	public void send(ActorMessage<?> message, ActorServiceNode node, String path);
	public void priority(ActorMessage<?> message);
	public void unhandled(ActorMessage<?> message);
	
	public UUID addChild(ActorFactory factory);
	public List<UUID> addChild(ActorFactory factory, int instances);
	
	public void preStart();
	public void restart(Exception reason);
	public void stop();
	public void watch(UUID dest);
	public void unwatch(UUID dest);
	
	public <E extends ActorPersistenceObject> void persist(Consumer<E> onSuccess, Consumer<Exception> onFailure, @SuppressWarnings("unchecked") E... events);
	public <S extends ActorPersistenceObject> void saveSnapshot(Consumer<S> onSuccess, Consumer<Exception> onFailure, S state);
}
