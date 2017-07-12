/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.examples.matcher;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorMessageMatcher;

import static actor4j.core.utils.ActorLogger.*;

public class MyActor extends Actor {
	protected ActorMessageMatcher matcher;
	protected final int ACK = 1;
	
	@Override
	public void preStart() {
		matcher = new ActorMessageMatcher();
		
		matcher
			.match(String.class, 
				msg -> logger().info(String.format(
						"Received String message: %s", msg.valueAsString())))
			.match(ACK, 
				msg -> logger().info("ACK tag received"))
			.matchAny(
				msg -> send(msg, msg.dest))
			.matchElse(
				msg -> unhandled(msg));
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		matcher.apply(message);
	}
}
