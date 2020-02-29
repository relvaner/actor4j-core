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
package io.actor4j.core.actors;

import java.util.UUID;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorEmbeddedRouter;

public abstract class EmbeddedHostActor extends Actor {
	protected ActorEmbeddedRouter router;
	protected boolean redirectEnabled;
	
	public EmbeddedHostActor() {
		this(null, false);
	}
	
	public EmbeddedHostActor(String name) {
		this(name, false);
	}
	
	public EmbeddedHostActor(boolean redirectEnabled) {
		this(null, redirectEnabled);
	}
	
	public EmbeddedHostActor(String name, boolean redirectEnabled) {
		super(name);
		
		this.redirectEnabled = redirectEnabled;
		this.router = new ActorEmbeddedRouter();
	}
	
	public ActorEmbeddedRouter getRouter() {
		return router;
	}
	
	public UUID addEmbeddedChild(EmbeddedActor embeddedActor) {
		embeddedActor.host = this;
		router.put(embeddedActor.getId(), embeddedActor);
		if (redirectEnabled)
			getSystem().addRedirection(embeddedActor.getId(), self());
		
		return embeddedActor.getId();
	}
	
	public void removeEmbeddedChild(EmbeddedActor embeddedActor) {
		embeddedActor.host = null;
		router.remove(embeddedActor.id);
		if (redirectEnabled)
			getSystem().removeRedirection(embeddedActor.getId());
	}	
	
	public boolean embedded(ActorMessage<?> message) {
		boolean result = false;
		
		EmbeddedActor embeddedActor = router.get(message.dest);
		if (embeddedActor!=null)
			result = embeddedActor.embedded(message);
		else if (message.dest.equals(self()))
			receive(message);
		
		return result;
	}
	
	public <T> boolean embedded(T value, int tag, UUID dest) {
		boolean result = false;
		
		EmbeddedActor embeddedActor = router.get(dest);
		if (embeddedActor!=null)
			result = embeddedActor.embedded(value, tag, self(), dest);
		else if (dest.equals(self()))
			receive(new ActorMessage<T>(value, tag, self(), dest));
		
		return result;
	}
	
	public void sendWithinHost(ActorMessage<?> message) {
		EmbeddedActor embeddedActor = router.get(message.dest);
		if (embeddedActor!=null)
			embeddedActor.embedded(message.copy());
		else if (message.dest.equals(self()))
			receive(message.copy());
	}
	
	@Override
	public void postStop() {
		for (EmbeddedActor embeddedActor : router.values())
			if (redirectEnabled)
				getSystem().removeRedirection(embeddedActor.getId());
	}
}
