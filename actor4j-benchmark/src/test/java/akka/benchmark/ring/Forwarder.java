package akka.benchmark.ring;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.benchmark.ActorMessage;

public class Forwarder extends UntypedActor {
    protected ActorRef next;
    
    public Forwarder() {
    	this(null);
    }
    
    public Forwarder(ActorRef next) {
    	super();
    	this.next = next;
    }
	
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof ActorMessage) {
			if (next!=null)
				next.tell(message, getSelf());
			else
				((ActorRef)((ActorMessage) message).value).tell(message, getSelf());	
		}
	}
}
