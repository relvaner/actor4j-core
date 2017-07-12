package akka.benchmark.ping.pong.bulk;

import static akka.benchmark.ping.pong.bulk.ActorMessageTag.*;

import java.util.concurrent.atomic.AtomicLong;

import akka.actor.UntypedActor;
import akka.benchmark.ActorMessage;

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
