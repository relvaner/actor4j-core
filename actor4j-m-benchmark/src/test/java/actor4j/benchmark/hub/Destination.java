package actor4j.benchmark.hub;

import static actor4j.benchmark.hub.ActorMessageTag.MSG;

import actor4j.core.Actor;
import actor4j.core.ActorMessage;

public class Destination extends Actor {
	public Destination() {
		super();
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==MSG.ordinal()) {
			message.dest = message.source;
			message.source = getId();
			send(message);
		}
	}
}
