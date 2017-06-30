package akka.benchmark.ping.pong;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.benchmark.ActorMessage;

public class HubPattern {
	protected List<ActorRef> ports;

	public HubPattern() {
		ports = new ArrayList<ActorRef>();
	}
	
	public void add(ActorRef ref) {
		ports.add(ref);
	}
	
	public void broadcast(ActorMessage message, ActorRef source) {
		for (ActorRef dest : ports)
			dest.tell(message, source);
	}
}
