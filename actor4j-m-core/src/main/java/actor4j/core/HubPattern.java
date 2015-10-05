package actor4j.core;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class HubPattern {
	protected ActorSystem system;
	
	protected List<UUID> ports;

	public HubPattern(ActorSystem system) {
		super();
		
		this.system = system;
		
		ports = new LinkedList<>();
	}
	
	public HubPattern(ActorSystem system, ActorGroup group) {
		this(system);
		
		ports.addAll(group);
	}
	
	public void add(UUID id) {
		ports.add(id);
	}
	
	public void addAll(ActorGroup group) {
		ports.addAll(group);
	}
	
	public void broadcast(ActorMessage<?> message) {
		for (UUID id : ports) {
			message.dest = id;
			system.messagePassing.post(message);
		}
	}
}
