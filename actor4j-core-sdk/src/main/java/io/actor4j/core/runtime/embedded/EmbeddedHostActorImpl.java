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
package io.actor4j.core.runtime.embedded;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.actor4j.core.utils.ActorUtils.actorLabel;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorSystemError;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.di.DIContainer;
import io.actor4j.core.runtime.di.DefaultDIContainer;
import io.actor4j.core.utils.EmbeddedActorFactory;

public class EmbeddedHostActorImpl {
	protected final ActorRef host;
	
	protected final DIContainer<ActorId> container;
	protected final EmbeddedActorStrategyOnFailure actorStrategyOnFailure;
	
	protected final ActorEmbeddedRouter<ActorId> router;     // ActorId  -> InternalEmbeddedActorCell
	protected final ActorEmbeddedRouter<UUID> handlerRouter; // GlobalId -> InternalEmbeddedActorCell
	protected final boolean redirectEnabled;

	protected final Queue<ActorMessage<?>> messageQueue;
	protected final boolean messageQueueEnabled;
	
	protected /*quasi final*/ Consumer<ActorMessage<?>> callbackHost;
	protected /*quasi final*/ BiConsumer<InternalEmbeddedActorCell, Exception> fallbackHost;
	
	public EmbeddedHostActorImpl(ActorRef host) {
		this(host, false, false);
	}
	
	public EmbeddedHostActorImpl(ActorRef host, boolean redirectEnabled) {
		this(host, redirectEnabled, false);
	}
	
	public EmbeddedHostActorImpl(ActorRef host, boolean redirectEnabled, boolean messageQueueEnabled) {
		super();
		
		this.host = host;
		
		container = DefaultDIContainer.create();
		actorStrategyOnFailure = new DefaultEmbeddedActorStrategyOnFailure(this);
		
		this.redirectEnabled = redirectEnabled;
		this.router = new ActorEmbeddedRouter<>();
		this.handlerRouter = new ActorEmbeddedRouter<>();
		
		messageQueue = new LinkedList<>(); /* unbounded */
		this.messageQueueEnabled = messageQueueEnabled;
		
		fallbackHost = (embeddedActorCell, e) -> actorStrategyOnFailure.handle(embeddedActorCell, e);
	}
	
	public ActorId self() {
		return host.getId();
	}
	
	public ActorRef getHost() {
		return host;
	}

	public DIContainer<ActorId> getContainer() {
		return container;
	}

	public ActorEmbeddedRouter<ActorId> getRouter() {
		return router;
	}
	
	public ActorEmbeddedRouter<UUID> getHandlerRouter() {
		return handlerRouter;
	}

	public Consumer<ActorMessage<?>> getCallbackHost() {
		return callbackHost;
	}

	public void setCallbackHost(Consumer<ActorMessage<?>> callbackHost) {
		this.callbackHost = callbackHost;
	}

	public BiConsumer<InternalEmbeddedActorCell, Exception> getFallbackHost() {
		return fallbackHost;
	}

	public void setFallbackHost(BiConsumer<InternalEmbeddedActorCell, Exception> fallbackHost) {
		this.fallbackHost = fallbackHost;
	}

	public boolean isEmbedded(ActorId id) {
		return router.get(id)!=null;
	}

	protected InternalEmbeddedActorCell createEmbeddedActorCell(EmbeddedActor embeddedActor, UUID globalId) {
		return new BaseEmbeddedActorCell(host, embeddedActor, globalId);
	}
	
	public ActorId addEmbeddedChild(EmbeddedActorFactory factory) {
		return addEmbeddedChild(factory, UUID.randomUUID());
	}
	
	public ActorId addEmbeddedChild(EmbeddedActorFactory factory, UUID globalId) {
		InternalEmbeddedActorCell embeddedActorCell = createEmbeddedActorCell(factory.create(), globalId);
		container.register(embeddedActorCell.getId(), factory);
		
		router.put(embeddedActorCell.getId(), embeddedActorCell);
		handlerRouter.put(globalId, embeddedActorCell);
		if (redirectEnabled)
			host.getSystem().addRedirection(embeddedActorCell.getId(), self());
		
		embeddedActorCell.getActor().setCell(embeddedActorCell);
		try {
			embeddedActorCell.preStart();
		}
		catch(Exception e) {
			((InternalActorSystem)host.getSystem()).getExecutorService().getFaultToleranceManager()
				.notifyErrorHandler(e, ActorSystemError.EMBEDDED_ACTOR, actorLabel(embeddedActorCell.getActor()), host.getId());
			if (fallbackHost!=null)
				fallbackHost.accept(embeddedActorCell, e);
		}
		
		return embeddedActorCell.getId();
	}
	
	public void removeEmbeddedChild(ActorId id) {
		InternalEmbeddedActorCell embeddedActorCell = router.get(id);
		
		if (embeddedActorCell!=null) {
			embeddedActorCell.postStop();
			
			router.remove(id);
			handlerRouter.remove(id.globalId());
			if (redirectEnabled)
				host.getSystem().removeRedirection(id);

			container.unregister(id);
			embeddedActorCell.getActor().setCell(null);
		}
	}
	
	protected boolean faultToleranceMethod(ActorMessage<?> message, InternalEmbeddedActorCell embeddedActorCell) {
		boolean result = false;
		
		try {
			result = embeddedActorCell.embedded(message);
		}
		catch(Exception e) {
			((InternalActorSystem)host.getSystem()).getExecutorService().getFaultToleranceManager()
				.notifyErrorHandler(e, ActorSystemError.EMBEDDED_ACTOR, actorLabel(embeddedActorCell.getActor()), host.getId());
			if (fallbackHost!=null)
				fallbackHost.accept(embeddedActorCell, e);
		}
		
		return result;
	}
	
	public boolean embedded(ActorMessage<?> message) {
		boolean result = false;
		
		if (message==null)
			throw new NullPointerException();
		
		InternalEmbeddedActorCell embeddedActorCell = router.get(message.dest());
		if (embeddedActorCell!=null)
			result = faultToleranceMethod(message.copy(), embeddedActorCell);
		
		internal_embedded();
		
		return result;
	}
	
	public boolean embedded(ActorMessage<?> message, ActorId dest) {
		boolean result = false;
		
		if (message==null)
			throw new NullPointerException();
		
		InternalEmbeddedActorCell embeddedActorCell = router.get(dest);
		if (embeddedActorCell!=null)
			result = faultToleranceMethod(message.copy(dest), embeddedActorCell);
		
		internal_embedded();
		
		return result;
	}
	
	public void embedded() {
		internal_embedded();
	}
	
	protected void internal_embedded() {
		if (messageQueueEnabled) {
			ActorMessage<?> message = null;
			while((message=messageQueue.poll())!=null) {
				InternalEmbeddedActorCell embeddedActorCell = router.get(message.dest());
				if (embeddedActorCell!=null)
					faultToleranceMethod(message, embeddedActorCell);
				else if (message.dest().equals(self()) && callbackHost!=null)
					callbackHost.accept(message);
			}
		}
	}
	
	public void sendWithinHost(ActorMessage<?> message) {
		if (messageQueueEnabled) 
			messageQueue.offer(message.copy());
		else {
			InternalEmbeddedActorCell embeddedActorCell = router.get(message.dest());
			if (embeddedActorCell!=null)
				faultToleranceMethod(message.copy(), embeddedActorCell);
			else if (message.dest().equals(self()) && callbackHost!=null)
				callbackHost.accept(message.copy());
		}
	}
	
	public void sendUnsafeWithinHost(ActorMessage<?> message) {
		InternalEmbeddedActorCell embeddedActorCell = router.get(message.dest());
		if (embeddedActorCell!=null)
			faultToleranceMethod(message.copy(), embeddedActorCell);
		else if (message.dest().equals(self()) && callbackHost!=null)
			callbackHost.accept(message.copy());
	}

	public void postStop() {
		for (InternalEmbeddedActorCell embeddedActorCell : router.values())
			embeddedActorCell.stop();
	}
}
