package actor4j.core;

import actor4j.supervisor.AllForOneSupervisorStrategy;
import actor4j.supervisor.OneForOneSupervisorStrategy;
import actor4j.supervisor.SupervisorStrategyDirective;

import static actor4j.core.ActorLogger.logger;
import static actor4j.core.ActorUtils.actorLabel;

public class ActorStrategyOnFailure {
	protected void oneForOne_directive_resume(Actor actor) {
		logger().info(String.format("System - actor (%s) resumed", actorLabel(actor)));
	}
	
	protected void oneForOne_directive_restart(Actor actor) {
		actor.preRestart();
		// actor.stop(); ... with DI
		actor.postRestart();
		logger().info(String.format("System - actor (%s) restarted", actorLabel(actor))); 
	}
	
	protected void oneForOne_directive_stop(Actor actor) {
		actor.stop();
		logger().info(String.format("System - actor (%s) stopped", actorLabel(actor)));
	}
	
	protected void allForOne_directive_resume(Actor actor) {
		oneForOne_directive_resume(actor);
	}
	
	public void handle(Actor actor, Exception e) {
		if (actor.supervisorStrategy()==null)
			oneForOne_directive_restart(actor);
		else {
			SupervisorStrategyDirective directive = actor.supervisorStrategy().apply(e);
			if (actor.supervisorStrategy() instanceof OneForOneSupervisorStrategy) { 
				if (directive==SupervisorStrategyDirective.RESUME)
					oneForOne_directive_resume(actor);
				else if (directive==SupervisorStrategyDirective.RESTART)
					oneForOne_directive_restart(actor);
				else if (directive==SupervisorStrategyDirective.STOP)
					oneForOne_directive_stop(actor);
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
}
