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
package io.actor4j.core.internal;

import static io.actor4j.core.utils.ActorUtils.*;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import io.actor4j.core.ActorCell;
import io.actor4j.core.ActorServiceNode;
import io.actor4j.core.messages.ActorMessage;

public class DefaultActorMessageDispatcher extends ActorMessageDispatcher {
	protected final Consumer<ActorMessage<?>> consumerPseudo;
	
	protected final BiPredicate<ActorMessage<?>, Queue<ActorMessage<?>>> antiFloodingStrategy;
	
	public DefaultActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
		consumerPseudo = new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> msg) {
				ActorCell cell = DefaultActorMessageDispatcher.this.system.getPseudoCells().get(msg.dest());
				if (cell!=null)
					((PseudoActorCell)cell).getOuterQueue().offer(msg);
			}
		};
		
		// see weighted random early discard (WRED) strategy, currently not used
		antiFloodingStrategy = new BiPredicate<ActorMessage<?>, Queue<ActorMessage<?>>>() {
			protected Random random = new Random();
			@Override
			public boolean test(ActorMessage<?> message, Queue<ActorMessage<?>> queue) {
				boolean result = false;
				
				if (!isDirective(message)) {
					int bound = (int)(queue.size()/(double)system.config.queueSize*10);
					if (bound>=8)
						result = true;
					else if (bound>=2)
						result = (random.nextInt(10)>=10-bound);
					//result = queue.size()>25000;
				}
				
				return result;
			}
		};
	}
	
	@Override
	public void post(ActorMessage<?> message, UUID source, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		UUID dest = message.dest();
		
		if (alias!=null) {
			List<UUID> destinations = system.getActorsFromAlias(alias);

			dest = null;
			if (!destinations.isEmpty()) {
				if (destinations.size()==1)
					dest = destinations.get(0);
				else
					dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
			}
			if (dest==null)
				dest = UUID_ALIAS;
		}
		
		UUID redirect = system.redirector.get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (system.pseudoCells.containsKey(dest)) {
			consumerPseudo.accept(message.copy(dest));
			return;
		}
		else if (system.config.clientMode && !system.cells.containsKey(dest)) {
			system.executerService.clientViaAlias(message.copy(dest), alias);
			return;
		}
		else if (system.resourceCells.containsKey(dest)) {
			system.executerService.resource(message.copy(dest));
			return;
		}
		
		if (alias==null && redirect==null)
			system.executerService.actorThreadPool.actorThreadPoolHandler.postInnerOuter(message, source);
		else
			system.executerService.actorThreadPool.actorThreadPoolHandler.postInnerOuter(message, source, dest);
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
		
		UUID dest = message.dest();
		
		UUID redirect = system.redirector.get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.resourceCells.containsKey(dest)) {
				system.executerService.resource(message.copy());
				return;
			}
			
			if (!system.executerService.actorThreadPool.actorThreadPoolHandler.postQueue(message, biconsumer)) 
				consumerPseudo.accept(message.copy());
		}
		else {
			if (system.resourceCells.containsKey(dest)) {
				system.executerService.resource(message.copy(dest));
				return;
			}
			
			if (!system.executerService.actorThreadPool.actorThreadPoolHandler.postQueue(message, dest, biconsumer)) 
				consumerPseudo.accept(message.copy(dest));
		}
	}
	
	@Override
	public void postOuter(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		UUID dest = message.dest();
		
		UUID redirect = system.redirector.get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.resourceCells.containsKey(dest)) {
				system.executerService.resource(message.copy());
				return;
			}
			
			if (!system.executerService.actorThreadPool.actorThreadPoolHandler.postOuter(message))
				consumerPseudo.accept(message.copy());
		}
		else {
			if (system.resourceCells.containsKey(dest)) {
				system.executerService.resource(message.copy(dest));
				return;
			}
			
			if (!system.executerService.actorThreadPool.actorThreadPoolHandler.postOuter(message, dest))
				consumerPseudo.accept(message.copy(dest));
		}
	}
	
	@Override
	public void postServer(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		UUID dest = message.dest();
		
		UUID redirect = system.redirector.get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.resourceCells.containsKey(dest)) {
				system.executerService.resource(message.copy());
				return;
			}
			
			if (!system.executerService.actorThreadPool.actorThreadPoolHandler.postServer(message))
				consumerPseudo.accept(message.copy());
		}
		else {
			if (system.resourceCells.containsKey(dest)) {
				system.executerService.resource(message.copy(dest));
				return;
			}
			
			if (!system.executerService.actorThreadPool.actorThreadPoolHandler.postServer(message, dest))
				consumerPseudo.accept(message.copy(dest));
		}
	}
	/*
	@Override
	public void postServer(ActorMessage<?> message) {
		postQueue(message, (t, msg) -> t.serverQueue(message));
	}
	*/
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
