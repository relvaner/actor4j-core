/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.embedded.ActorEmbeddedRouter;
import io.actor4j.core.runtime.embedded.EmbeddedHostActorImpl;
import io.actor4j.core.utils.EmbeddedActorFactory;

public abstract class EmbeddedHostActor extends Actor {
	protected final EmbeddedHostActorImpl impl;
	
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
		
		impl = new EmbeddedHostActorImpl(this, redirectEnabled, messageQueueEnabled);
	}
	
	public EmbeddedHostActorImpl underlyingImpl() {
		return impl;
	}

	public ActorEmbeddedRouter getRouter() {
		return impl.getRouter();
	}
	
	public boolean isEmbedded(ActorId id) {
		return impl.getRouter().get(id)!=null;
	}
	
	public ActorId addEmbeddedChild(EmbeddedActorFactory factory) {
		return impl.addEmbeddedChild(factory);
	}

	public ActorId addEmbeddedChild(EmbeddedActorFactory factory, ActorId id) {
		return impl.addEmbeddedChild(factory, id);
	}
	
	public void removeEmbeddedChild(ActorId id) {
		impl.removeEmbeddedChild(id);
	}
	
	public boolean embedded(ActorMessage<?> message) {
		return impl.embedded(message);
	}
	
	public boolean embedded(ActorMessage<?> message, ActorId dest) {
		return impl.embedded(message, dest);
	}
	
	public <T> boolean embedded(T value, int tag, ActorId dest) {
		return embedded(ActorMessage.create(value, tag, self(), dest));
	}
	
	public void sendWithinHost(ActorMessage<?> message) {
		impl.sendWithinHost(message);
	}

	@Override
	public void postStop() {
		impl.postStop();
		
		super.postStop();
	}
}
