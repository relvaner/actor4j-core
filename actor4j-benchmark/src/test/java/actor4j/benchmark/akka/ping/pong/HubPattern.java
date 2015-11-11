package actor4j.benchmark.akka.ping.pong;

import java.util.ArrayList;
import java.util.List;

import actor4j.benchmark.akka.ActorMessage;
import akka.actor.ActorRef;

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
