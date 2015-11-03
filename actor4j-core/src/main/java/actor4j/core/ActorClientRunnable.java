/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import actor4j.core.messages.ActorMessage;

public interface ActorClientRunnable {
	public void run(ActorMessage<?> message, String alias);
}
