/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.supervisor;

public abstract class OneForAllSupervisorStrategy extends SupervisorStrategy {
	public OneForAllSupervisorStrategy(int maxRetries, long withinTimeRange) {
		super(maxRetries, withinTimeRange);
	}
}
