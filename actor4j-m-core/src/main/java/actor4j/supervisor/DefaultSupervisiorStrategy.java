package actor4j.supervisor;

import actor4j.core.ActorInitializationException;
import static actor4j.supervisor.SupervisorStrategyDirective.*;

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
