package actor4j.core;

import actor4j.supervisor.AllForOneSupervisorStrategy;
import actor4j.supervisor.OneForOneSupervisorStrategy;
import actor4j.supervisor.SupervisorStrategyDirective;

public class ActorStrategyOnFailure {
	protected void oneForOne_directive_resume(Actor actor) {
		ActorLogger.logger().info(
				String.format("System - actor (%s) resumed", 
						actor.getName()!=null ? actor.getName() : actor.getId().toString())
				); 
	}
	
	protected void oneForOne_directive_restart(Actor actor) {
		actor.preRestart();
		// actor.stop(); ... with DI
		actor.postRestart();
		ActorLogger.logger().info(
				String.format("System - actor (%s) restarted", 
						actor.getName()!=null ? actor.getName() : actor.getId().toString())
				); 
	}
	
	protected void oneForOne_directive_stop(Actor actor) {
		actor.stop();
		ActorLogger.logger().info(
				String.format("System - actor (%s) stopped", 
						actor.getName()!=null ? actor.getName() : actor.getId().toString())
				); 
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
