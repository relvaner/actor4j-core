/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.actors;

import java.util.UUID;

import actor4j.core.utils.ActorGroup;

public abstract class ActorWithGroup extends Actor implements ActorGroupMember {
	protected UUID groupId;
	
	public ActorWithGroup(ActorGroup group) {
		this(null, group);
	}
	
	public ActorWithGroup(String name, ActorGroup group) {
		super(name);
		
		groupId = group.getId();
		//group.add(self());
	}

	@Override
	public UUID getGroupId() {
		return groupId;
	}
}
