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

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;

public interface ActorCell extends ActorId {
	public ActorSystem getSystem();
	
	public ActorId localId();
	public UUID globalId();
	
	public int getType();
	public boolean isPod();
	public ActorId getId();
	public ActorId getParent();
	public Queue<ActorId> getChildren();
	
	public boolean isRoot();
	public boolean isRootInUser();
	
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace);
	public void unbecome();
	public void unbecomeAll();
	
	public void send(ActorMessage<?> message);
	public void send(ActorMessage<?> message, String alias);
	public void priority(ActorMessage<?> message);
	public void unhandled(ActorMessage<?> message);
	
	public ActorId addChild(ActorFactory factory);
	public List<ActorId> addChild(ActorFactory factory, int instances);
	
	public void preStart();
	public void restart(Exception reason);
	public void stop();
	public void watch(ActorId dest);
	public void unwatch(ActorId dest);
	
	public <E> void persist(Consumer<E> onSuccess, Consumer<Exception> onFailure, @SuppressWarnings("unchecked") E... events);
	public <S> void saveSnapshot(Consumer<S> onSuccess, Consumer<Exception> onFailure, S state);
	
	public static int DEFAULT_ACTOR_CELL  = 0;
	public static int RESOURCE_ACTOR_CELL = 1;
	public static int PSEUDO_ACTOR_CELL   = 2;
}
