package actor4j.supervisor;

public abstract class AllForOneSupervisorStrategy extends SupervisorStrategy {
	public AllForOneSupervisorStrategy(int maxRetries, long withinTimeRange) {
		super(maxRetries, withinTimeRange);
	}
}
