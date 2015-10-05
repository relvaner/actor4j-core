package actor4j.benchmark.akka.bcast;

import actor4j.benchmark.akka.ActorMessage;
import akka.actor.UntypedActor;

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
