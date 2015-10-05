package actor4j.benchmark.akka.hub;

import java.util.concurrent.atomic.AtomicLong;

import actor4j.benchmark.akka.ActorMessage;
import akka.actor.UntypedActor;
import static actor4j.benchmark.akka.hub.ActorMessageTag.*;

public class Destination extends UntypedActor {
    protected AtomicLong counter;
    
    public Destination(AtomicLong counter) {
    	this.counter = counter;
    }
	 
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof ActorMessage)
			if (((ActorMessage)message).tag==MSG.ordinal()) {
				getSender().tell(message, getSelf());
				counter.getAndIncrement();
				counter.getAndIncrement();
			}
	}
}
