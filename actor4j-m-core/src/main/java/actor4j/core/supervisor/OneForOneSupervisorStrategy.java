/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.supervisor;

public abstract class OneForOneSupervisorStrategy extends SupervisorStrategy {
	public OneForOneSupervisorStrategy(int maxRetries, long withinTimeRange) {
		super(maxRetries, withinTimeRange);
	}
}
