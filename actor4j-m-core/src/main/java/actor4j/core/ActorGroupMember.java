package actor4j.core;

import java.util.UUID;

public abstract class ActorGroupMember extends Actor {
	protected UUID groupId;
	
	public ActorGroupMember(ActorGroup group) {
		this(null, group);
	}
	
	public ActorGroupMember(String name, ActorGroup group) {
		super(name);
		
		groupId = group.getId();
		group.add(getId());
	}

	public UUID getGroupId() {
		return groupId;
	}
}
