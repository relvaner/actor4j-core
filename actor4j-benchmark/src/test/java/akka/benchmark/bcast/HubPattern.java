package akka.benchmark.bcast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import akka.actor.ActorRef;
import akka.benchmark.ActorMessage;

public class HubPattern {
	protected List<ActorRef> ports;
	
	protected AtomicLong count;

	public HubPattern() {
		ports = new ArrayList<ActorRef>();
		
		count = new AtomicLong();
	}
	
	public void add(ActorRef ref) {
		ports.add(ref);
	}
	
	public void broadcast(ActorMessage message, ActorRef source) {
		count.getAndIncrement();
		for (ActorRef dest : ports)
			dest.tell(message, source);
	}
	
	public long getCount() {
		return count.get();
	}
}
