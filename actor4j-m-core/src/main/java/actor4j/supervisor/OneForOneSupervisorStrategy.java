package actor4j.supervisor;

public abstract class OneForOneSupervisorStrategy extends SupervisorStrategy {
	public OneForOneSupervisorStrategy(int maxRetries, long withinTimeRange) {
		super(maxRetries, withinTimeRange);
	}
}
