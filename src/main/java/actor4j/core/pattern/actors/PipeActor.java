/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actor4j.core.pattern.actors;

import java.util.UUID;
import java.util.function.BiFunction;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class PipeActor extends Actor {
	protected BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler;
	protected UUID next;
	
	public PipeActor(BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler, UUID next) {
		this(null, handler, next);
	}
	
	public PipeActor(String name, BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler, UUID next) {
		super(name);
		
		this.handler = handler;
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (handler!=null)
			send(handler.apply(this, message), next);
	}
}
