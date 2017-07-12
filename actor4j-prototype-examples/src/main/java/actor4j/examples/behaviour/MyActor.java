/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.examples.behaviour;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

import static actor4j.core.utils.ActorLogger.*;

public class MyActor extends Actor {
	protected final int SWAP=22;
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag == SWAP)
			become(msg -> {
				logger().info(String.format(
						"Received String message: %s", msg.valueAsString()));
				unbecome();
			}, false);
		else
			unhandled(message);
	}
}
