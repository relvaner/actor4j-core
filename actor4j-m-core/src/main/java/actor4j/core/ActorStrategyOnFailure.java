package actor4j.core;

public class ActorStrategyOnFailure {
	public void handle(Actor actor, Exception e) {
		if (actor.supervisorStrategy()==null)
			actor.preRestart();
			// actor.stop(); ... with DI
			actor.postRestart();
			ActorLogger.logger().info(
					String.format("System - actor (%s) restarted", 
							actor.getName()!=null ? actor.getName() : actor.getId().toString())
					); 
	}
}
