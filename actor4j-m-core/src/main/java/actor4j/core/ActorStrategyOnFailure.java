package actor4j.core;

import static actor4j.core.ActorLogger.logger;
import static actor4j.core.ActorProtocolTag.*;
import static actor4j.core.ActorUtils.actorLabel;

import java.util.Iterator;
import java.util.UUID;

import actor4j.core.supervisor.AllForOneSupervisorStrategy;
import actor4j.core.supervisor.OneForOneSupervisorStrategy;
import actor4j.core.supervisor.SupervisorStrategy;
import actor4j.core.supervisor.SupervisorStrategyDirective;

public class ActorStrategyOnFailure {
	protected ActorSystem system;
	
	public ActorStrategyOnFailure(ActorSystem system) {
		this.system = system;
	}
	
	protected void oneForOne_directive_resume(Actor actor) {
		logger().info(String.format("System - actor (%s) resumed", actorLabel(actor)));
	}
	
	protected void oneForOne_directive_restart(Actor actor, Exception reason) {
		actor.preRestart(reason);
	}
	
	protected void oneForOne_directive_stop(Actor actor) {
		actor.stop();
	}
	
	protected void allForOne_directive_resume(Actor actor) {
		oneForOne_directive_resume(actor);
	}
	
	protected void allForOne_directive_restart(Actor actor, Exception reason) {
		if (!actor.isRoot()) {
			Actor parent = system.actors.get(actor.getParent());
			if (parent!=null) {
				Iterator<UUID> iterator = parent.getChildren().iterator();
				while (iterator.hasNext()) {
					UUID dest = iterator.next();
					if (!dest.equals(actor.getId()))
						actor.send(new ActorMessage<>(reason, INTERNAL_RESTART, parent.getId(), dest));
				}
				actor.preRestart(reason);
			}
		}
		else 
			actor.preRestart(reason);
	}
	
	protected void allForOne_directive_stop(Actor actor) {
		if (!actor.isRoot()) {
			Actor parent = system.actors.get(actor.getParent());
			if (parent!=null) {
				Iterator<UUID> iterator = parent.getChildren().iterator();
				while (iterator.hasNext()) {
					UUID dest = iterator.next();
					if (!dest.equals(actor.getId()))
						actor.send(new ActorMessage<>(null, INTERNAL_STOP, parent.getId(), dest));
				}
				actor.stop();
			}
		}
		else 
			actor.stop();
	}
	
	public void handle(Actor actor, Exception e) {
		SupervisorStrategy supervisorStrategy = system.actors.get(actor.parent).supervisorStrategy();
		SupervisorStrategyDirective directive = supervisorStrategy.apply(e);
		
		if (actor.supervisorStrategy() instanceof OneForOneSupervisorStrategy) { 
			if (directive==SupervisorStrategyDirective.RESUME)
				oneForOne_directive_resume(actor);
			else if (directive==SupervisorStrategyDirective.RESTART)
				oneForOne_directive_restart(actor, e);
			else if (directive==SupervisorStrategyDirective.STOP)
				oneForOne_directive_stop(actor);
			else if (directive==SupervisorStrategyDirective.ESCALATE)
				;
		}
		else if (actor.supervisorStrategy() instanceof AllForOneSupervisorStrategy) { 
			if (directive==SupervisorStrategyDirective.RESUME)
				allForOne_directive_resume(actor);
			else if (directive==SupervisorStrategyDirective.RESTART)
				allForOne_directive_restart(actor, e);
			else if (directive==SupervisorStrategyDirective.STOP)
				allForOne_directive_stop(actor);
			else if (directive==SupervisorStrategyDirective.ESCALATE)
				;
		}
	}
}
