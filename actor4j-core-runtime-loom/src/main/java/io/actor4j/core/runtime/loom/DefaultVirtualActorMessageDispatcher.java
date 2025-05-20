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
package io.actor4j.core.runtime.loom;

import static io.actor4j.core.logging.ActorLogger.WARN;
import static io.actor4j.core.logging.ActorLogger.systemLogger;
import static io.actor4j.core.utils.ActorUtils.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import io.actor4j.core.ActorCell;
import io.actor4j.core.actors.PseudoActor;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorMessageDispatcher;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.PseudoActorCell;

public class DefaultVirtualActorMessageDispatcher extends ActorMessageDispatcher {
	protected final Function<ActorMessage<?>, Boolean> consumerPseudo;
	
	public DefaultVirtualActorMessageDispatcher(InternalActorSystem system) {
		super(system);
		
		consumerPseudo = new Function<ActorMessage<?>, Boolean>() {
			@Override
			public Boolean apply(ActorMessage<?> msg) {
				boolean result = false;
				
				ActorCell cell = DefaultVirtualActorMessageDispatcher.this.system.getPseudoCells().get(msg.dest());
				if (cell!=null) {
					((PseudoActorCell)cell).getOuterQueue().offer(msg);
					result = true;
				}
				
				return result;
			}
		};
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
		
		InternalActorCell cell = (InternalActorCell)dest;
		ActorId redirect = cell.getRedirect();
		if (redirect!=null) 
			dest = redirect;
		
		if (cell.getType()==ActorCell.DEFAULT_ACTOR_CELL) {
			if (alias==null && redirect==null)
				dispatch(message, null, false, true);
			else
				dispatch(message, dest, false, true);
		}
		else if (cell.getType()==ActorCell.RESOURCE_ACTOR_CELL) {
			system.getExecutorService().resource(message.copy(dest));
		}
		else if (cell.getType()==ActorCell.PSEUDO_ACTOR_CELL) {
			consumerPseudo.apply(message.copy(dest));
		}
		else
			undelivered(message, source, dest);
	}
	
	protected void postQueue(ActorMessage<?> message, boolean directive) {
		if (message==null)
			throw new NullPointerException();
		
		ActorId dest = message.dest();
		
		InternalActorCell cell = (InternalActorCell)dest;
		ActorId redirect = cell.getRedirect();
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (cell.getType()==ActorCell.DEFAULT_ACTOR_CELL) {
				dispatch(message, null, directive, true);
			}
			else if (cell.getType()==ActorCell.RESOURCE_ACTOR_CELL) {
				system.getExecutorService().resource(message.copy());
			}
			else if (cell.getType()==ActorCell.PSEUDO_ACTOR_CELL) {
				consumerPseudo.apply(message.copy());
			}
			else
				undelivered(message, message.source(), dest);
		}
		else {
			if (cell.getType()==ActorCell.DEFAULT_ACTOR_CELL) {
				dispatch(message, dest, directive, true);
			}
			else if (cell.getType()==ActorCell.RESOURCE_ACTOR_CELL) {
				system.getExecutorService().resource(message.copy(dest));
			}
			else if (cell.getType()==ActorCell.PSEUDO_ACTOR_CELL) {
				consumerPseudo.apply(message.copy(dest));
			}
			else
				undelivered(message, message.source(), dest);
		}
	}
	
	public boolean dispatch(ActorMessage<?> message, ActorId dest, boolean directive, boolean debugUndelivered) {
		boolean result = false;
		
		VirtualActorRunnablePoolHandler handler = ((InternalVirtualActorExecutorService)system.getExecutorService()).getVirtualActorRunnablePool().getVirtualActorRunnablePoolHandler();
		if (dest!=null)
			result = handler.post(message, dest, directive, debugUndelivered);
		else
			result = handler.post(message, directive, debugUndelivered);
		
		return result;
	}
	
	@Override
	public void postOuter(ActorMessage<?> message) {
		postQueue(message, false);
	}
	
	@Override
	public void postServer(ActorMessage<?> message) {
		postQueue(message, false);
	}
	
	@Override
	public void postPriority(ActorMessage<?> message) {
		postQueue(message, false);
	}
	
	@Override
	public void postDirective(ActorMessage<?> message) {
		postQueue(message, true);
	}

	@Override
	public void postPersistence(ActorMessage<?> message) {
		((InternalVirtualActorExecutorService)system.getExecutorService()).getVirtualActorRunnablePool().getVirtualActorRunnablePoolHandler().postPersistence(message);
	}
	
	@Override
	public void undelivered(ActorMessage<?> message, ActorId source, ActorId dest) {
		if (system.getConfig().debugUndelivered()) {
			InternalActorCell cell = (InternalActorCell)source;
		
			dispatch(message.shallowCopy(dest), system.UNKNOWN_ID(), false, false);
			systemLogger().log(WARN,
				String.format("[UNDELIVERED] Message (%s) from source (%s) - Unavailable actor (%s)",
					message.toString(), cell!=null ? actorLabel(cell.getActor()) : source.toString(), dest
				));
		}
	}
	
	@Override
	public void registerCell(InternalActorCell cell) {
		if (cell.getActor() instanceof ResourceActor)
			((InternalVirtualActorExecutorService)system.getExecutorService()).getVirtualActorResourceRunnablePool().registerCell(cell);
		else
			((InternalVirtualActorExecutorService)system.getExecutorService()).getVirtualActorRunnablePool().registerCell(cell);
	}
	
	@Override
	public void unregisterCell(InternalActorCell cell) {
		if (!(cell.getActor() instanceof PseudoActor)) {
			if (cell.getActor() instanceof ResourceActor)
				((InternalVirtualActorExecutorService)system.getExecutorService()).getVirtualActorResourceRunnablePool().unregisterCell(cell);
			else
				((InternalVirtualActorExecutorService)system.getExecutorService()).getVirtualActorRunnablePool().unregisterCell(cell);
		}
	}
	
	@Override
	public boolean isRegisteredCell(InternalActorCell cell) {
		return ((InternalVirtualActorExecutorService)system.getExecutorService()).getVirtualActorRunnablePool().isRegisteredCell(cell);
	}

	@Override
	public void unsafe_post(ActorMessage<?> message, ActorId source, String alias) {
		// TODO Auto-generated method stub
		
	}
}
