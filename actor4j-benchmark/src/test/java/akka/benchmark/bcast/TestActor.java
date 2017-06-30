package akka.benchmark.bcast;

import akka.actor.UntypedActor;
import akka.benchmark.ActorMessage;

public class TestActor extends UntypedActor {
	protected HubPattern hub;
	
	public TestActor(HubPattern hub) {
		super();
		
		this.hub = hub;
	}

	@Override
	public void onReceive(Object message) throws Exception {
        if (message instanceof ActorMessage)
        	hub.broadcast((ActorMessage)message, getSelf());
	}
}
