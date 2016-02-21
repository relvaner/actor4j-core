/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class ActorGroup extends HashSet<UUID> {
	protected static final long serialVersionUID = -7544316988654909201L;
	
	protected UUID id;
	
	public ActorGroup() {
		super();
		
		id = UUID.randomUUID();
	}
	
	public ActorGroup(Collection<? extends UUID> c) {
		super(c);
		
		id = UUID.randomUUID();
	}

	public UUID getId() {
		return id;
	}

	protected void setId(UUID id) {
		this.id = id;
	}
}
