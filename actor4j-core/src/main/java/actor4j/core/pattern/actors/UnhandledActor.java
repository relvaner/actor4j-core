/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.pattern.actors;

import java.util.function.Consumer;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class UnhandledActor extends Actor {
	protected Consumer<ActorMessage<?>> handler;
	
	public UnhandledActor(Consumer<ActorMessage<?>> handler) {
		this(null, handler);
	}
	
	public UnhandledActor(String name, Consumer<ActorMessage<?>> handler) {
		super(name);
		
		this.handler = handler;
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		handler.accept(message);
	}
}
