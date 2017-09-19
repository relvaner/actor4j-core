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
package actor4j.core;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import actor4j.core.ActorMessageDispatcher;
import actor4j.core.ActorSystemImpl;
import actor4j.core.messages.ActorMessage;
import static actor4j.core.utils.ActorUtils.*;

public class DefaultActorMessageDispatcher extends ActorMessageDispatcher {
	protected BiConsumer<Long, ActorMessage<?>> biconsumerServer;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerPriority;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerDirective;
	
	protected Consumer<ActorMessage<?>> consumerPseudo;
	
	protected BiPredicate<ActorMessage<?>, Queue<ActorMessage<?>>> antiFloodingStrategy;
	
	public DefaultActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
		biconsumerServer = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((DefaultActorThread)threadsMap.get(id_dest)).serverQueueL2.offer(msg);
				((DefaultActorThread)threadsMap.get(id_dest)).newMessage();
			}
		};
		biconsumerPriority = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((DefaultActorThread)threadsMap.get(id_dest)).priorityQueue.offer(msg);
				((DefaultActorThread)threadsMap.get(id_dest)).newMessage();
			}
		};
		biconsumerDirective = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((DefaultActorThread)threadsMap.get(id_dest)).directiveQueue.offer(msg);
				((DefaultActorThread)threadsMap.get(id_dest)).newMessage();
			}
		};
		
		consumerPseudo = new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> msg) {
				ActorCell cell = DefaultActorMessageDispatcher.this.system.pseudoCells.get(msg.dest);
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
					int bound = (int)(queue.size()/(double)system.queueSize*10);
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
		
		if (system.parallelismMin==1 && system.parallelismFactor==1) {
			((DefaultActorThread)Thread.currentThread()).innerQueue.offer(message.copy());
			((DefaultActorThread)Thread.currentThread()).newMessage();
		}
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(message.dest);
			
			if (id_dest!=null) {
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					((DefaultActorThread)threadsMap.get(id_dest)).innerQueue.offer(message.copy());
				else
					((DefaultActorThread)threadsMap.get(id_dest)).outerQueueL2.offer(message.copy());
				
				((DefaultActorThread)threadsMap.get(id_dest)).newMessage();
			}	
		}
	}
	
	public void post(ActorMessage<?> message, ActorServiceNode node, String path) {
		if (message==null)
			throw new NullPointerException();
		
		if (node!=null && path!=null)
			system.executerService.clientViaPath(message, node, path);
	}
	
	protected void postQueue(ActorMessage<?> message, BiConsumer<Long, ActorMessage<?>> biconsumer) {
		if (message==null)
			throw new NullPointerException();
		
		UUID redirect = system.redirector.get(message.dest);
		if (redirect!=null) 
			message.dest = redirect;
		
		if (system.resourceCells.containsKey(message.dest)) {
			system.executerService.resource(message.copy());
			return;
		}
		
		Long id_dest = cellsMap.get(message.dest);
		if (id_dest!=null)
			biconsumer.accept(id_dest, message.copy());
		else 
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
		
		Long id_dest = cellsMap.get(message.dest);
		if (id_dest!=null) {
			((DefaultActorThread)threadsMap.get(id_dest)).outerQueueL2.offer(message.copy());
			((DefaultActorThread)threadsMap.get(id_dest)).newMessage();
		}
		else 
			consumerPseudo.accept(message.copy());
	}
	
	@Override
	public void postServer(ActorMessage<?> message) {
		postQueue(message, biconsumerServer);
	}
	
	@Override
	public void postPriority(ActorMessage<?> message) {
		postQueue(message, biconsumerPriority);
	}
	
	@Override
	public void postDirective(ActorMessage<?> message) {
		postQueue(message, biconsumerDirective);
	}

	@Override
	public void postPersistence(ActorMessage<?> message) {
		Long id_source = cellsMap.get(message.source);
		message.dest = system.executerService.persistenceService.getService().getActorFromAlias(persistenceMap.get(id_source));
		system.executerService.persistenceService.getService().send(message.copy());
	}
}
