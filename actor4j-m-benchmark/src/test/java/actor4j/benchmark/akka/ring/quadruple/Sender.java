package actor4j.benchmark.akka.ring.quadruple;

import java.util.concurrent.atomic.AtomicLong;

import actor4j.benchmark.akka.ActorMessage;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class Sender extends UntypedActor {
    protected AtomicLong counter;
    protected ActorRef next;
    protected int size;
    
    protected int i;
    
    public Sender(AtomicLong counter, ActorRef next, int size) {
    	super();
    	this.counter = counter;
    	this.next = next;
    	this.size = size;
    }
	 
	@Override
	public void onReceive(Object message) throws Exception {
		next.tell(new ActorMessage(getSelf(), 0), getSelf());
		counter.addAndGet(size);
	}
}
