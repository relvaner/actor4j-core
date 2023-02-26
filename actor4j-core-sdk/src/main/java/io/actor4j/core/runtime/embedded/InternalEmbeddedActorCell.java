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
package io.actor4j.core.runtime.embedded;

import java.util.function.Predicate;

import io.actor4j.core.EmbeddedActorCell;
import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.supervisor.SupervisorStrategy;

public interface InternalEmbeddedActorCell extends EmbeddedActorCell {
	public EmbeddedActor getActor();
	public void setActor(EmbeddedActor actor);
	
	public boolean isActive();
	public void setActive(boolean active);
	
	public boolean embedded(ActorMessage<?> message);
	public void become(Predicate<ActorMessage<?>> behaviour);
	public void fireActiveBehaviour(ActorMessage<?> message, Predicate<Integer> condition);
	
	public void unsafe_send(ActorMessage<?> message);
	
	public SupervisorStrategy getParentSupervisorStrategy();
	public void setParentSupervisorStrategy(SupervisorStrategy parentSupervisorStrategy);
	
	public void preRestart(Exception reason);
	public void postRestart(Exception reason);
	public void postStop();
}
