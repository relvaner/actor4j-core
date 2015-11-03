package actor4j.core;

import java.util.UUID;

import actor4j.core.messages.ActorMessage;

public class ActorService extends ActorSystem {
	public ActorService() {
		this(null);
	}
	
	public ActorService(String name) {
		super(name);
	}
	
	public boolean hasActor(String uuid) {
		return system.hasActor(uuid);
	}
	
	public UUID getActor(String alias) {
		return system.getActor(alias);
	}
	
	public void sendAsServer(ActorMessage<?> message) {
		system.sendAsServer(message);
	}
}
