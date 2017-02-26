/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core;

import static actor4j.core.protocols.ActorProtocolTag.*;
import static actor4j.core.supervisor.SupervisorStrategyDirective.*;
import static actor4j.core.utils.ActorLogger.logger;
import static actor4j.core.utils.ActorUtils.actorLabel;

import java.util.Iterator;
import java.util.UUID;

import actor4j.core.messages.ActorMessage;
import actor4j.core.supervisor.OneForAllSupervisorStrategy;
import actor4j.core.supervisor.OneForOneSupervisorStrategy;
import actor4j.core.supervisor.SupervisorStrategy;
import actor4j.core.supervisor.SupervisorStrategyDirective;

public class ActorStrategyOnFailure {
	protected ActorSystemImpl system;
	
	public ActorStrategyOnFailure(ActorSystemImpl system) {
		this.system = system;
	}
	
	protected void oneForOne_directive_resume(ActorCell cell) {
		logger().info(String.format("%s - System: actor (%s) resumed", cell.system.name, actorLabel(cell.actor)));
	}
	
	protected void oneForOne_directive_restart(ActorCell cell, Exception reason) {
		cell.preRestart(reason);
	}
	
	protected void oneForOne_directive_stop(ActorCell cell) {
		cell.stop();
	}
	
	protected void oneForAll_directive_resume(ActorCell cell) {
		oneForOne_directive_resume(cell);
	}
	
	protected void oneForAll_directive_restart(ActorCell cell, Exception reason) {
		if (!cell.isRoot()) {
			ActorCell parent = system.cells.get(cell.parent);
			if (parent!=null) {
				Iterator<UUID> iterator = parent.children.iterator();
				while (iterator.hasNext()) {
					UUID dest = iterator.next();
					if (!dest.equals(cell.id))
						system.sendAsDirective(new ActorMessage<>(reason, INTERNAL_RESTART, parent.id, dest));
				}
				cell.preRestart(reason);
			}
		}
		else 
			cell.preRestart(reason);
	}
	
	protected void oneForAll_directive_stop(ActorCell cell) {
		if (!cell.isRoot()) {
			ActorCell parent = system.cells.get(cell.parent);
			if (parent!=null) {
				Iterator<UUID> iterator = parent.children.iterator();
				while (iterator.hasNext()) {
					UUID dest = iterator.next();
					if (!dest.equals(cell.id))
						system.sendAsDirective(new ActorMessage<>(null, INTERNAL_STOP, parent.id, dest));
				}
				cell.stop();
			}
		}
		else 
			cell.stop();
	}
	
	public void handle(ActorCell cell, Exception e) {
		ActorCell parent = system.cells.get(cell.parent);
		SupervisorStrategy supervisorStrategy = parent.supervisorStrategy();
		SupervisorStrategyDirective directive = supervisorStrategy.apply(e);
		
		while (directive==ESCALATE && !parent.isRoot()) {
			parent = system.cells.get(parent);
			supervisorStrategy = parent.supervisorStrategy();
			directive = supervisorStrategy.apply(e);
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
