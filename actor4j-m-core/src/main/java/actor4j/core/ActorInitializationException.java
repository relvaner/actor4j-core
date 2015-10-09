package actor4j.core;

public class ActorInitializationException extends RuntimeException {
	protected static final long serialVersionUID = -6130654059639276742L;

	public ActorInitializationException() {
		super("actor initialization failed");
	}
}
