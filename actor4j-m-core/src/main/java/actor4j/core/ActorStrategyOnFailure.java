package actor4j.core;

import actor4j.supervisor.AllForOneSupervisorStrategy;
import actor4j.supervisor.OneForOneSupervisorStrategy;
import actor4j.supervisor.SupervisorStrategy;
import actor4j.supervisor.SupervisorStrategyDirective;

import static actor4j.core.ActorLogger.logger;
import static actor4j.core.ActorUtils.actorLabel;

import java.util.UUID;

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
		UUID buf = actor.getId();
		try {
			actor = (Actor)system.container.getInstance(buf);
			actor.setId(buf);	
			system.actors.put(buf, actor);
			actor.postRestart(reason);
			logger().info(String.format("System - actor (%s) restarted", actorLabel(actor))); 
		} catch (Exception e) {
			throw new ActorInitializationException(); // never must occur
		}
	}
	
	protected void oneForOne_directive_stop(Actor actor) {
		actor.stop();
		logger().info(String.format("System - actor (%s) stopped", actorLabel(actor)));
	}
	
	protected void allForOne_directive_resume(Actor actor) {
		oneForOne_directive_resume(actor);
	}
	
	public void handle(Actor actor, Exception e) {
		SupervisorStrategy supervisorStrategy = system.actors.get(actor.parent).supervisorStrategy();
		SupervisorStrategyDirective directive = supervisorStrategy.apply(e);
		
		if (actor.supervisorStrategy() instanceof OneForOneSupervisorStrategy) { 
			if (directive==SupervisorStrategyDirective.RESUME)
				oneForOne_directive_resume(actor);
			else if (directive==SupervisorStrategyDirective.RESTART)
				; //oneForOne_directive_restart(actor, e);
			else if (directive==SupervisorStrategyDirective.STOP)
				; //oneForOne_directive_stop(actor);
			else if (directive==SupervisorStrategyDirective.ESCALATE)
				;
		}
		else if (actor.supervisorStrategy() instanceof AllForOneSupervisorStrategy) { 
			if (directive==SupervisorStrategyDirective.RESUME)
				allForOne_directive_resume(actor);
			else if (directive==SupervisorStrategyDirective.RESTART)
				;
			else if (directive==SupervisorStrategyDirective.STOP)
				;
			else if (directive==SupervisorStrategyDirective.ESCALATE)
				;
		}
	}
}
