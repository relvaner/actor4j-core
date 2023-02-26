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

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.supervisor.SupervisorStrategyDirective.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.supervisor.OneForAllSupervisorStrategy;
import io.actor4j.core.supervisor.OneForOneSupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategyDirective;

public class DefaultEmbeddedActorStrategyOnFailure implements EmbeddedActorStrategyOnFailure {
	protected final EmbeddedHostActorImpl hostImpl;
	
	public DefaultEmbeddedActorStrategyOnFailure(EmbeddedHostActorImpl hostImpl) {
		this.hostImpl = hostImpl;
	}
	
	protected void oneForOne_directive_resume(InternalEmbeddedActorCell cell) {
		systemLogger().log(INFO, String.format("[LIFECYCLE] embedded actor (%s) resumed", actorLabel(cell.getActor())));
	}
	
	protected void oneForOne_directive_restart(InternalEmbeddedActorCell cell, Exception reason) {
		cell.preRestart(reason);
	}
	
	protected void oneForOne_directive_stop(InternalEmbeddedActorCell cell) {
		cell.stop();
	}
	
	protected void oneForAll_directive_resume(InternalEmbeddedActorCell cell) {
		oneForOne_directive_resume(cell);
	}
	
	protected void oneForAll_directive_restart(InternalEmbeddedActorCell cell, Exception reason) {
		for (InternalEmbeddedActorCell embeddedActorCell : hostImpl.getRouter().values())
			embeddedActorCell.preRestart(reason);
	}
	
	protected void oneForAll_directive_stop(InternalEmbeddedActorCell cell) {
		for (InternalEmbeddedActorCell embeddedActorCell : hostImpl.getRouter().values())
			embeddedActorCell.stop();
	}
	
	@Override
	public void handle(InternalEmbeddedActorCell cell, Exception e) {
		if (cell.getParentSupervisorStrategy()==null)	
			cell.setParentSupervisorStrategy(((InternalActorCell)((Actor)hostImpl.getHost()).getCell()).supervisorStrategy());
		
		SupervisorStrategy supervisorStrategy = cell.getParentSupervisorStrategy();
		SupervisorStrategyDirective directive = supervisorStrategy.handle(e);
		
		if (supervisorStrategy instanceof OneForOneSupervisorStrategy) { 
			if (directive==RESUME)
				oneForOne_directive_resume(cell);
			else if (directive==RESTART)
				oneForOne_directive_restart(cell, e);
			else if (directive==STOP)
				oneForOne_directive_stop(cell);
		}
		else if (supervisorStrategy instanceof OneForAllSupervisorStrategy) { 
			if (directive==RESUME)
				oneForAll_directive_resume(cell);
			else if (directive==RESTART)
				oneForAll_directive_restart(cell, e);
			else if (directive==STOP)
				oneForAll_directive_stop(cell);
		}
	}
}
