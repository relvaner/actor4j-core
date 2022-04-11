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
package io.actor4j.core.runtime;

import static io.actor4j.core.logging.ActorLogger.WARN;
import static io.actor4j.core.logging.ActorLogger.systemLogger;
import static io.actor4j.core.utils.ActorUtils.*;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import io.actor4j.core.ActorCell;
import io.actor4j.core.internal.ActorMessageDispatcher;
import io.actor4j.core.internal.ActorThread;
import io.actor4j.core.internal.InternalActorCell;
import io.actor4j.core.internal.InternalActorSystem;
import io.actor4j.core.messages.ActorMessage;

public class DefaultActorMessageDispatcher extends ActorMessageDispatcher {
	protected final Function<ActorMessage<?>, Boolean> consumerPseudo;
	
	protected final BiPredicate<ActorMessage<?>, Queue<ActorMessage<?>>> antiFloodingStrategy;
	
	public DefaultActorMessageDispatcher(InternalActorSystem system) {
		super(system);
		
		consumerPseudo = new Function<ActorMessage<?>, Boolean>() {
			@Override
			public Boolean apply(ActorMessage<?> msg) {
				boolean result = false;
				
				ActorCell cell = DefaultActorMessageDispatcher.this.system.getPseudoCells().get(msg.dest());
				if (cell!=null) {
					((PseudoActorCell)cell).getOuterQueue().offer(msg);
					result = true;
				}
				
				return result;
			}
		};
		
		// see weighted random early discard (WRED) strategy, currently not used
		antiFloodingStrategy = new BiPredicate<ActorMessage<?>, Queue<ActorMessage<?>>>() {
			protected Random random = new Random();
			@Override
			public boolean test(ActorMessage<?> message, Queue<ActorMessage<?>> queue) {
				boolean result = false;
				
				if (!isDirective(message)) {
					int bound = (int)(queue.size()/(double)system.getConfig().queueSize()*10);
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
				dest = ALIAS_ID;
		}
		
		UUID redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (system.getPseudoCells().containsKey(dest)) {
			consumerPseudo.apply(message.copy(dest));
			return;
		}
		else if (system.getResourceCells().containsKey(dest)) {
			system.getExecuterService().resource(message.copy(dest));
			return;
		}
		
		if (alias==null && redirect==null)
			((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postInnerOuter(message, source);
		else
			((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postInnerOuter(message, source, dest);
	}
	
	protected void postQueue(ActorMessage<?> message, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		if (message==null)
			throw new NullPointerException();
		
		UUID dest = message.dest();
		
		UUID redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecuterService().resource(message.copy());
				return;
			}
			
			if (!((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postQueue(message, biconsumer)) 
				if (!consumerPseudo.apply(message.copy()))
					undelivered(message, message.source(), message.dest());
		}
		else {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecuterService().resource(message.copy(dest));
				return;
			}
			
			if (!((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postQueue(message, dest, biconsumer)) 
				if (!consumerPseudo.apply(message.copy(dest)))
					undelivered(message, message.source(), dest);
		}
	}
	
	@Override
	public void postOuter(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		UUID dest = message.dest();
		
		UUID redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecuterService().resource(message.copy());
				return;
			}
			
			if (!((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postOuter(message))
				if (!consumerPseudo.apply(message.copy()))
					undelivered(message, message.source(), message.dest());
		}
		else {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecuterService().resource(message.copy(dest));
				return;
			}
			
			if (!((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postOuter(message, dest))
				if (!consumerPseudo.apply(message.copy(dest)))
					undelivered(message, message.source(), dest);
		}
	}
	
	@Override
	public void postServer(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		UUID dest = message.dest();
		
		UUID redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecuterService().resource(message.copy());
				return;
			}
			
			if (!((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postServer(message))
				if (!consumerPseudo.apply(message.copy()))
					undelivered(message, message.source(), message.dest());
		}
		else {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecuterService().resource(message.copy(dest));
				return;
			}
			
			if (!((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postServer(message, dest))
				if (!consumerPseudo.apply(message.copy(dest)))
					undelivered(message, message.source(), dest);
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
		((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postPersistence(message);
	}
	
	@Override
	public void undelivered(ActorMessage<?> message, UUID source, UUID dest) {
		if (system.getConfig().debugUndelivered()) {
			InternalActorCell cell = system.getCells().get(source);
		
			((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().postOuter(message.shallowCopy(dest), system.UNKNOWN_ID());
			systemLogger().log(WARN,
				String.format("[UNDELIVERED] Message (%s) from source (%s) - Unavailable actor (%s)",
					message.toString(), cell!=null ? actorLabel(cell.getActor()) : source.toString(), dest
				));
		}
	}
	
	@Override
	public void registerCell(InternalActorCell cell) {
		((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().registerCell(cell);
	}
	
	@Override
	public void unregisterCell(InternalActorCell cell) {
		((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().unregisterCell(cell);
	}
	
	@Override
	public boolean isRegisteredCell(InternalActorCell cell) {
		return ((DefaultActorExecuterService)system.getExecuterService()).getActorThreadPool().getActorThreadPoolHandler().isRegisteredCell(cell);
	}
}
