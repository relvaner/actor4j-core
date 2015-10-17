/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import static actor4j.core.ActorProtocolTag.*;
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
	protected ActorSystem system;
	
	public ActorStrategyOnFailure(ActorSystem system) {
		this.system = system;
	}
	
	protected void oneForOne_directive_resume(Actor actor) {
		logger().info(String.format("%s - System: actor (%s) resumed", actor.system.name, actorLabel(actor)));
	}
	
	protected void oneForOne_directive_restart(Actor actor, Exception reason) {
		actor.preRestart(reason);
	}
	
	protected void oneForOne_directive_stop(Actor actor) {
		actor.stop();
	}
	
	protected void oneForAll_directive_resume(Actor actor) {
		oneForOne_directive_resume(actor);
	}
	
	protected void oneForAll_directive_restart(Actor actor, Exception reason) {
		if (!actor.isRoot()) {
			Actor parent = system.actors.get(actor.getParent());
			if (parent!=null) {
				Iterator<UUID> iterator = parent.getChildren().iterator();
				while (iterator.hasNext()) {
					UUID dest = iterator.next();
					if (!dest.equals(actor.getId()))
						system.sendAsDirective(new ActorMessage<>(reason, INTERNAL_RESTART, parent.getId(), dest));
				}
				actor.preRestart(reason);
			}
		}
		else 
			actor.preRestart(reason);
	}
	
	protected void oneForAll_directive_stop(Actor actor) {
		if (!actor.isRoot()) {
			Actor parent = system.actors.get(actor.getParent());
			if (parent!=null) {
				Iterator<UUID> iterator = parent.getChildren().iterator();
				while (iterator.hasNext()) {
					UUID dest = iterator.next();
					if (!dest.equals(actor.getId()))
						system.sendAsDirective(new ActorMessage<>(null, INTERNAL_STOP, parent.getId(), dest));
				}
				actor.stop();
			}
		}
		else 
			actor.stop();
	}
	
	public void handle(Actor actor, Exception e) {
		Actor parent = system.actors.get(actor.parent);
		SupervisorStrategy supervisorStrategy = parent.supervisorStrategy();
		SupervisorStrategyDirective directive = supervisorStrategy.apply(e);
			
		if (supervisorStrategy instanceof OneForOneSupervisorStrategy) { 
			if (directive==RESUME)
				oneForOne_directive_resume(actor);
			else if (directive==RESTART)
				oneForOne_directive_restart(actor, e);
			else if (directive==STOP)
				oneForOne_directive_stop(actor);
		}
		else if (supervisorStrategy instanceof OneForAllSupervisorStrategy) { 
			if (directive==RESUME)
				oneForAll_directive_resume(actor);
			else if (directive==RESTART)
				oneForAll_directive_restart(actor, e);
			else if (directive==STOP)
				oneForAll_directive_stop(actor);
		}
	}
}
