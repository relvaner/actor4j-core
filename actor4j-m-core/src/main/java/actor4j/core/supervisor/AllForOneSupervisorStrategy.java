/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.supervisor;

public abstract class AllForOneSupervisorStrategy extends SupervisorStrategy {
	public AllForOneSupervisorStrategy(int maxRetries, long withinTimeRange) {
		super(maxRetries, withinTimeRange);
	}
}
