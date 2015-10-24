/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.actors;

import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.utils.ActorGroup;

public abstract class ActorGroupMember extends Actor {
	protected UUID groupId;
	
	public ActorGroupMember(ActorGroup group) {
		this(null, group);
	}
	
	public ActorGroupMember(String name, ActorGroup group) {
		super(name);
		
		groupId = group.getId();
		group.add(self());
	}

	public UUID getGroupId() {
		return groupId;
	}
}
