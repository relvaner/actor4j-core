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

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorEmbeddedRouter;

public abstract class EmbeddedHostActor extends Actor {
	protected final ActorEmbeddedRouter router;
	protected final boolean redirectEnabled;
	
	protected final Queue<ActorMessage<?>> messageQueue;
	protected final boolean messageQueueEnabled;
	
	public EmbeddedHostActor() {
		this(null, false, false);
	}
	
	public EmbeddedHostActor(String name) {
		this(name, false, false);
	}
	
	public EmbeddedHostActor(boolean redirectEnabled) {
		this(null, redirectEnabled, false);
	}
	
	public EmbeddedHostActor(boolean redirectEnabled, boolean messageQueueEnabled) {
		this(null, redirectEnabled, messageQueueEnabled);
	}
	
	public EmbeddedHostActor(String name, boolean redirectEnabled, boolean messageQueueEnabled) {
		super(name);
		
		this.redirectEnabled = redirectEnabled;
		this.router = new ActorEmbeddedRouter();
		
		messageQueue = new LinkedList<>(); /* unbounded */
		this.messageQueueEnabled = messageQueueEnabled;
	}
	
	public ActorEmbeddedRouter getRouter() {
		return router;
	}
	
	public boolean isEmbedded(UUID id) {
		return router.get(id)!=null;
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
		
		if (message==null)
			throw new NullPointerException();
		
		EmbeddedActor embeddedActor = router.get(message.dest());
		if (embeddedActor!=null)
			result = embeddedActor.embedded(message);
		else if (message.dest().equals(self()))
			receive(message);
		
		internal_embedded();
		
		return result;
	}
	
	public <T> boolean embedded(T value, int tag, UUID dest) {
		return embedded(ActorMessage.create(value, tag, self(), dest));
	}
	
	public void embedded() {
		internal_embedded();
	}
	
	protected void internal_embedded() {
		if (messageQueueEnabled) {
			ActorMessage<?> message = null;
			while((message=messageQueue.poll())!=null) {
				EmbeddedActor embeddedActor = router.get(message.dest());
				if (embeddedActor!=null)
					embeddedActor.embedded(message);
			}
		}
	}
	
	public void sendWithinHost(ActorMessage<?> message) {
		if (messageQueueEnabled) 
			messageQueue.offer(message);
		else {
			EmbeddedActor embeddedActor = router.get(message.dest());
			if (embeddedActor!=null)
				embeddedActor.embedded(message.copy());
			else if (message.dest().equals(self()))
				receive(message.copy());
		}
	}
	
	@Override
	public void postStop() {
		for (EmbeddedActor embeddedActor : router.values())
			if (redirectEnabled)
				getSystem().removeRedirection(embeddedActor.getId());
	}
}
