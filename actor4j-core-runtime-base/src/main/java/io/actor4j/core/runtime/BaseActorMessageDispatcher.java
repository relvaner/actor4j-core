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
package io.actor4j.core.runtime;

import io.actor4j.core.actors.PseudoActor;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.messages.ActorMessage;

public abstract class BaseActorMessageDispatcher extends ActorMessageDispatcher {
	public BaseActorMessageDispatcher(InternalActorSystem system) {
		super(system);
	}
	
	@Override
	public void postPersistence(ActorMessage<?> message) {
		((InternalActorExecutorService<?>)system.getExecutorService()).getExecutionUnitPool().getExecutionUnitPoolHandler().postPersistence(message);
	}
	
	@Override
	public void registerCell(InternalActorCell cell) {
		if (!(cell.getActor() instanceof ResourceActor))
			((InternalActorExecutorService<?>)system.getExecutorService()).getExecutionUnitPool().getExecutionUnitPoolHandler().registerCell(cell);
	}
	
	@Override
	public void unregisterCell(InternalActorCell cell) {
		if (!(cell.getActor() instanceof ResourceActor) && !(cell.getActor() instanceof PseudoActor))
			((InternalActorExecutorService<?>)system.getExecutorService()).getExecutionUnitPool().getExecutionUnitPoolHandler().unregisterCell(cell);
	}
	
	@Override
	public boolean isRegisteredCell(InternalActorCell cell) {
		return ((InternalActorExecutorService<?>)system.getExecutorService()).getExecutionUnitPool().getExecutionUnitPoolHandler().isRegisteredCell(cell);
	}
}
