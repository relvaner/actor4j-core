/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package cloud.actor4j.core;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import cloud.actor4j.core.ActorMessageDispatcher;
import cloud.actor4j.core.ActorSystemImpl;
import cloud.actor4j.core.messages.ActorMessage;

public class XActorMessageDispatcher extends ActorMessageDispatcher {
	protected final Consumer<ActorMessage<?>> consumerPseudo;
	
	public XActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
		consumerPseudo = new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> msg) {
				ActorCell cell = XActorMessageDispatcher.this.system.pseudoCells.get(msg.dest);
				if (cell!=null)
					((PseudoActorCell)cell).getOuterQueue().offer(msg);
			}
		};
	}
	
	@Override
	public void post(ActorMessage<?> message, UUID source, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		if (alias!=null) {
			List<UUID> destinations = system.getActorsFromAlias(alias);

			UUID dest = null;
			if (!destinations.isEmpty()) {
				if (destinations.size()==1)
					dest = destinations.get(0);
				else
					dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
			}
			message.dest = (dest!=null) ? dest : UUID_ALIAS;
		}
		
		UUID redirect = system.redirector.get(message.dest);
		if (redirect!=null) 
			message.dest = redirect;
		
		if (system.pseudoCells.containsKey(message.dest)) {
			consumerPseudo.accept(message.copy());
			return;
		}
		else if (system.clientMode && !system.cells.containsKey(message.dest)) {
			system.executerService.clientViaAlias(message.copy(), alias);
			return;
		}
		else if (system.resourceCells.containsKey(message.dest)) {
			system.executerService.resource(message.copy());
			return;
		}
		
		system.executerService.actorThreadPool.actorThreadPoolHandler.postInnerOuter(message, source);
	}
	
	public void post(ActorMessage<?> message, ActorServiceNode node, String path) {
		if (message==null)
			throw new NullPointerException();
		
		if (node!=null && path!=null)
			system.executerService.clientViaPath(message, node, path);
	}
	
	protected void postQueue(ActorMessage<?> message, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		if (message==null)
			throw new NullPointerException();
		
		UUID redirect = system.redirector.get(message.dest);
		if (redirect!=null) 
			message.dest = redirect;
		
		if (system.resourceCells.containsKey(message.dest)) {
			system.executerService.resource(message.copy());
			return;
		}
		
		if (!system.executerService.actorThreadPool.actorThreadPoolHandler.postQueue(message, biconsumer)) 
			consumerPseudo.accept(message.copy());
	}
	
	@Override
	public void postOuter(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		UUID redirect = system.redirector.get(message.dest);
		if (redirect!=null) 
			message.dest = redirect;
		
		if (system.resourceCells.containsKey(message.dest)) {
			system.executerService.resource(message.copy());
			return;
		}
		
		if (!system.executerService.actorThreadPool.actorThreadPoolHandler.postOuter(message))
			consumerPseudo.accept(message.copy());
	}
	
	@Override
	public void postServer(ActorMessage<?> message) {
		postQueue(message, (t, msg) -> t.serverQueue(message));
	}
	
	@Override
	public void postPriority(ActorMessage<?> message) {
		postQueue(message, (t, msg) -> t.priorityQueue(message));
	}
	
	@Override
	public void postDirective(ActorMessage<?> message) {
		postQueue(message, (t, msg) -> t.directiveQueue(message));
	}

	@Override
	public void postPersistence(ActorMessage<?> message) {
		system.executerService.actorThreadPool.actorThreadPoolHandler.postPersistence(message);
	}
}
