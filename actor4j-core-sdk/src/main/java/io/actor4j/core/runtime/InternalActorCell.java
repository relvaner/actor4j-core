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

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import io.actor4j.core.ActorCell;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.supervisor.SupervisorStrategy;

public interface InternalActorCell extends ActorCell {
	public Actor getActor();
	public void setActor(Actor actor);
	public void setParent(UUID parent);
	
	public boolean isActive();
	public void setActive(boolean active);
	public Queue<UUID> getDeathWatcher();
	public void setActiveDirectiveBehaviour(boolean activeDirectiveBehaviour);
	public boolean isRootInSystem();
	
	public void internal_receive(ActorMessage<?> message);
	public void become(Consumer<ActorMessage<?>> behaviour);
	
	public void unsafe_send(ActorMessage<?> message);
	public void unsafe_send(ActorMessage<?> message, String alias);
	
	public SupervisorStrategy supervisorStrategy();
	public SupervisorStrategy getParentSupervisorStrategy();
	public void setParentSupervisorStrategy(SupervisorStrategy parentSupervisorStrategy);
	
	public void preRestart(Exception reason);
	public void postRestart(Exception reason);
	public void postStop();
	public UUID internal_addChild(InternalActorCell cell);
	public void internal_stop();
	
	public AtomicLong getRequestRate();
	public Queue<Long> getProcessingTimeStatistics();
}
