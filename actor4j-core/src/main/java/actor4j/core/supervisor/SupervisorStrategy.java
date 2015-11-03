/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.supervisor;

public abstract class SupervisorStrategy {
	protected int retries;
	protected int maxRetries;
	protected long withinTimeRange;
	
	protected long startTime;
	protected long stopTime;
	
	public SupervisorStrategy(int maxRetries, long withinTimeRange) {
		this.maxRetries = maxRetries;
		this.withinTimeRange = withinTimeRange;
	}
	
	protected boolean isInTimeRange() {
		return (stopTime-startTime) <= withinTimeRange;
	}
	
	protected void reset() {
		retries   = 0;
		
		startTime = System.currentTimeMillis();
		stopTime  = startTime;
	}
	
	public SupervisorStrategyDirective handle(Exception e) {
		if (startTime==0)
			reset();
		else
			stopTime  = System.currentTimeMillis();
		
		if (!isInTimeRange())
			if (maxRetries>0 && retries>=maxRetries)
				return SupervisorStrategyDirective.STOP;
			else
				reset();
		
		retries++;
		
		return apply(e);
	}
	
	public abstract SupervisorStrategyDirective apply(Exception e);
}
