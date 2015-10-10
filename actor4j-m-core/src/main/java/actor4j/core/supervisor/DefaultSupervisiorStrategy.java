package actor4j.core.supervisor;

import static actor4j.core.supervisor.SupervisorStrategyDirective.*;

import actor4j.core.ActorInitializationException;

public class DefaultSupervisiorStrategy extends OneForOneSupervisorStrategy {
	public DefaultSupervisiorStrategy() {
		super(-1, Integer.MAX_VALUE);
	}

	@Override
	public SupervisorStrategyDirective apply(Exception e) {
		if (e instanceof ActorInitializationException)
			return STOP;
		else
			return RESTART;
	}

}
