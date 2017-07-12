/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.examples.starter;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

import static actor4j.core.utils.ActorLogger.*;

public class MyActor extends Actor {
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value instanceof String) {
			logger().info(String.format(
					"Received String message: %s", message.valueAsString()));
			send(message, message.source);
		} 
		else
			unhandled(message);
	}
}
