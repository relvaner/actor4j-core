/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.pattern.actors;

import java.util.UUID;
import java.util.function.BiFunction;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class BridgeActor extends PipeActor {
	public BridgeActor(BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler, UUID next) {
		super(handler, next);
	}
	
	public BridgeActor(String name, BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler, UUID next) {
		super(name, handler, next);
	}
}
