package actor4j.core;

import java.util.LinkedList;
import java.util.UUID;

public class ActorGroup  extends LinkedList<UUID> {
	protected static final long serialVersionUID = -7544316988654909201L;
	
	protected UUID id;
	
	public ActorGroup() {
		super();
		
		id = UUID.randomUUID();
	}

	public UUID getId() {
		return id;
	}

	protected void setId(UUID id) {
		this.id = id;
	}
}
