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
import java.util.concurrent.CompletableFuture;

import actor4j.core.actors.Actor;
import actor4j.core.actors.EmbeddedActor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.messages.FutureActorMessage;

public class FutureEmbeddedActor extends EmbeddedActor {
	protected CompletableFuture<Object> future;
	protected UUID dest;
	
	public FutureEmbeddedActor(UUID dest, Actor host) {
		this(null, dest, host);
	}
	
	public FutureEmbeddedActor(String name, UUID dest, Actor host) {
		super(name, host);
		this.dest = dest;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean receive(ActorMessage<?> message) {
		boolean result = true;
		
		if (message instanceof FutureActorMessage<?>) {
			future = ((FutureActorMessage<Object>)message).future;
			host.tell(message.value, message.tag, dest);
		}
		else if (message.source==dest) {
			future.complete(message.value);
		}
		else
			result = false;
		
		return result;
	}
}
