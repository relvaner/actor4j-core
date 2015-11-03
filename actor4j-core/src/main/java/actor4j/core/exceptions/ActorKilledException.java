package actor4j.core.exceptions;

public class ActorKilledException extends RuntimeException {
	protected static final long serialVersionUID = 5887686326593649655L;
	
	public ActorKilledException() {
		super("actor was killed");
	}
}
