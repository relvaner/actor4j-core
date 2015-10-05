package actor4j.server.example;

import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorMessage;

public class Sender extends Actor {
	protected UUID next;
	protected String alias;
	
	public Sender(UUID next) {
		super();
		
		this.next = next;
	}
	
	public Sender(String alias) {
		super();
		
		this.alias = alias;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		System.out.println(message);
		for (int i=0; i<10; i++)
			if (next!=null)
				send(new ActorMessage<UUID>(getId(), 1976+i, getId(), next));
			else
				send(new ActorMessage<UUID>(getId(), 1976+i, getId(), null), alias);
	}
}
