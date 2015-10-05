package actor4j.benchmark.ring.quadruple;

import java.util.UUID;

import actor4j.core.ActorGroup;
import actor4j.core.ActorGroupMember;
import actor4j.core.ActorMessage;

public class Forwarder extends ActorGroupMember {
	protected UUID next;
	
	protected long initalMessages;
	
	public Forwarder(ActorGroup group, UUID next) {
		super(group);
		
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (next!=null)
			send(message, next);
		else 
			send(message, message.valueAsUUID());
	}
}
