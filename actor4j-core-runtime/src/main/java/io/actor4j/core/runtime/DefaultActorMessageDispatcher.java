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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.actor4j.core.ActorCell;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public class DefaultActorMessageDispatcher extends BaseActorMessageDispatcher {
	protected final Function<ActorMessage<?>, Boolean> consumerPseudo;
	
//	protected final BiPredicate<ActorMessage<?>, Queue<ActorMessage<?>>> antiFloodingStrategy;
	
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
//		antiFloodingStrategy = new BiPredicate<ActorMessage<?>, Queue<ActorMessage<?>>>() {
//			protected Random random = new Random();
//			@Override
//			public boolean test(ActorMessage<?> message, Queue<ActorMessage<?>> queue) {
//				boolean result = false;
//				
//				if (!isDirective(message)) {
//					int bound = (int)(queue.size()/(double)system.getConfig().queueSize()*10);
//					if (bound>=8)
//						result = true;
//					else if (bound>=2)
//						result = (random.nextInt(10)>=10-bound);
//					//result = queue.size()>25000;
//				}
//				
//				return result;
//			}
//		};
	}
	
	protected ActorThreadPoolHandler getThreadPoolHandler() {
		return ((DefaultInternalActorExecutorService)system.getExecutorService()).getThreadPool().getThreadPoolHandler();
	}
	
	@Override
	public void unsafe_post(ActorMessage<?> message, ActorId source, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		ActorId dest = message.dest();
		
		if (alias!=null) {
			List<ActorId> destinations = system.getActorsFromAlias(alias);

			dest = null;
			if (!destinations.isEmpty()) {
				if (destinations.size()==1)
					dest = destinations.get(0);
				else
					dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
			}
			if (dest==null)
				dest = system.ALIAS_ID();
		}
		
		ActorId redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (alias==null && redirect==null)
			getThreadPoolHandler().unsafe_postInnerOuter(message, source);
		else
			getThreadPoolHandler().unsafe_postInnerOuter(message, source, dest);

	}
	
	@Override
	public void post(ActorMessage<?> message, ActorId source, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		ActorId dest = message.dest();
		
		if (alias!=null) {
			List<ActorId> destinations = system.getActorsFromAlias(alias);

			dest = null;
			if (!destinations.isEmpty()) {
				if (destinations.size()==1)
					dest = destinations.get(0);
				else
					dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
			}
			if (dest==null)
				dest = system.ALIAS_ID();
		}
		
		ActorId redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (system.getPseudoCells().containsKey(dest)) {
			consumerPseudo.apply(message.copy(dest));
			return;
		}
		else if (system.getResourceCells().containsKey(dest)) {
			system.getExecutorService().resource(message.copy(dest));
			return;
		}
		
		if (alias==null && redirect==null)
			getThreadPoolHandler().postInnerOuter(message, source);
		else
			getThreadPoolHandler().postInnerOuter(message, source, dest);
	}
	
	protected void postQueue(ActorMessage<?> message, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		if (message==null)
			throw new NullPointerException();
		
		ActorId dest = message.dest();
		
		ActorId redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecutorService().resource(message.copy());
				return;
			}
			
			if (!getThreadPoolHandler().postQueue(message, biconsumer)) 
				if (!consumerPseudo.apply(message.copy()))
					undelivered(message, message.source(), message.dest());
		}
		else {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecutorService().resource(message.copy(dest));
				return;
			}
			
			if (!getThreadPoolHandler().postQueue(message, dest, biconsumer)) 
				if (!consumerPseudo.apply(message.copy(dest)))
					undelivered(message, message.source(), dest);
		}
	}
	
	@Override
	public void postOuter(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		ActorId dest = message.dest();
		
		ActorId redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecutorService().resource(message.copy());
				return;
			}
			
			if (!getThreadPoolHandler().postOuter(message))
				if (!consumerPseudo.apply(message.copy()))
					undelivered(message, message.source(), message.dest());
		}
		else {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecutorService().resource(message.copy(dest));
				return;
			}
			
			if (!getThreadPoolHandler().postOuter(message, dest))
				if (!consumerPseudo.apply(message.copy(dest)))
					undelivered(message, message.source(), dest);
		}
	}
	
	@Override
	public void postServer(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		ActorId dest = message.dest();
		
		ActorId redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecutorService().resource(message.copy());
				return;
			}
			
			if (!getThreadPoolHandler().postServer(message))
				if (!consumerPseudo.apply(message.copy()))
					undelivered(message, message.source(), message.dest());
		}
		else {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecutorService().resource(message.copy(dest));
				return;
			}
			
			if (!getThreadPoolHandler().postServer(message, dest))
				if (!consumerPseudo.apply(message.copy(dest)))
					undelivered(message, message.source(), dest);
		}
	}
	
	/*
	@Override
	public void postServer(ActorMessage<?> message) {
		postQueue(message, (t, msg) -> t.serverQueue(msg));
	}
	*/
	
	@Override
	public void postPriority(ActorMessage<?> message) {
		postQueue(message, (t, msg) -> t.priorityQueue(msg));
	}
	
	@Override
	public void postDirective(ActorMessage<?> message) {
		postQueue(message, (t, msg) -> t.directiveQueue(msg));
	}
	
	@Override
	public void undelivered(ActorMessage<?> message, ActorId source, ActorId dest) {
		if (system.getConfig().debugUndelivered()) {
			InternalActorCell cell = (InternalActorCell)source;
		
			getThreadPoolHandler().postOuter(message.shallowCopy(dest), system.UNKNOWN_ID());
			systemLogger().log(WARN,
				String.format("[UNDELIVERED] Message (%s) from source (%s) - Unavailable actor (%s)",
					message.toString(), cell!=null ? actorLabel(cell.getActor()) : source.toString(), dest
				));
		}
	}
}
