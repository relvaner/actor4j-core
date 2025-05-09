/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.runtime.protocols.ActorProtocolTag.*;
import static io.actor4j.core.supervisor.SupervisorStrategyDirective.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.Iterator;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.supervisor.OneForAllSupervisorStrategy;
import io.actor4j.core.supervisor.OneForOneSupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategyDirective;

public class DefaultActorStrategyOnFailure implements ActorStrategyOnFailure {
	protected final InternalActorSystem system;
	
	public DefaultActorStrategyOnFailure(InternalActorSystem system) {
		this.system = system;
	}
	
	protected void oneForOne_directive_resume(InternalActorCell cell) {
		systemLogger().log(INFO, String.format("[LIFECYCLE] actor (%s) resumed", actorLabel(cell.getActor())));
	}
	
	protected void oneForOne_directive_restart(InternalActorCell cell, Exception reason) {
		cell.preRestart(reason);
	}
	
	protected void oneForOne_directive_stop(InternalActorCell cell) {
		cell.stop();
	}
	
	protected void oneForAll_directive_resume(InternalActorCell cell) {
		oneForOne_directive_resume(cell);
	}
	
	protected void oneForAll_directive_restart(InternalActorCell cell, Exception reason) {
		if (!cell.isRoot()) {
			InternalActorCell parent = system.getCells().get(cell.getParent());
			if (parent!=null) {
				Iterator<ActorId> iterator = parent.getChildren().iterator();
				while (iterator.hasNext()) {
					ActorId dest = iterator.next();
					if (!dest.equals(cell.getId()))
						system.sendAsDirective(ActorMessage.create(reason, INTERNAL_RESTART, parent.getId(), dest));
				}
				cell.preRestart(reason);
			}
		}
		else 
			cell.preRestart(reason);
	}
	
	protected void oneForAll_directive_stop(InternalActorCell cell) {
		if (!cell.isRoot()) {
			InternalActorCell parent = system.getCells().get(cell.getParent());
			if (parent!=null) {
				Iterator<ActorId> iterator = parent.getChildren().iterator();
				while (iterator.hasNext()) {
					ActorId dest = iterator.next();
					if (!dest.equals(cell.getId()))
						system.sendAsDirective(ActorMessage.create(null, INTERNAL_STOP, parent.getId(), dest));
				}
				cell.stop();
			}
		}
		else 
			cell.stop();
	}
	
	@Override
	public void handle(InternalActorCell cell, Exception e) {
		InternalActorCell parent = system.getCells().get(cell.getParent());
		if (cell.getParentSupervisorStrategy()==null)	
			cell.setParentSupervisorStrategy(parent.supervisorStrategy());
		
		SupervisorStrategy supervisorStrategy = cell.getParentSupervisorStrategy();
		SupervisorStrategyDirective directive = supervisorStrategy.handle(e);
		
		while (directive==ESCALATE && !parent.isRoot()) {
			parent = system.getCells().get(parent.getParent());
			supervisorStrategy = parent.supervisorStrategy();
			cell.setParentSupervisorStrategy(supervisorStrategy);
			directive = supervisorStrategy.handle(e);
		}
		
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
