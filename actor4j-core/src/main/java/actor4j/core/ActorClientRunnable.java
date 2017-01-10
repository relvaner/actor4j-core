/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core;

import actor4j.core.messages.ActorMessage;

public interface ActorClientRunnable {
	public void runViaAlias(ActorMessage<?> message, String alias);
	public void runViaPath(ActorMessage<?> message, ActorServiceNode node, String path);
}
