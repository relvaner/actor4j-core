/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.classic;

import static io.actor4j.core.logging.ActorLogger.WARN;
import static io.actor4j.core.logging.ActorLogger.systemLogger;
import static io.actor4j.core.utils.ActorUtils.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import io.actor4j.core.ActorCell;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.BaseActorMessageDispatcher;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.PseudoActorCell;

public class ClassicDefaultActorMessageDispatcher extends BaseActorMessageDispatcher implements ActorMessageDispatcherCallback {
	protected final Function<ActorMessage<?>, Boolean> consumerPseudo;
	
	public ClassicDefaultActorMessageDispatcher(InternalActorSystem system) {
		super(system);

		consumerPseudo = new Function<ActorMessage<?>, Boolean>() {
			@Override
			public Boolean apply(ActorMessage<?> msg) {
				boolean result = false;
				
				ActorCell cell = ClassicDefaultActorMessageDispatcher.this.system.getPseudoCells().get(msg.dest());
				if (cell!=null) {
					((PseudoActorCell)cell).getOuterQueue().offer(msg);
					result = true;
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
			system.getExecutorService().resource(message.copy(dest));
			return;
		}
		
		if (alias==null && redirect==null)
			dispatch(message, null, false, true);
		else
			dispatch(message, dest, false, true);
	}
	
	protected void postQueue(ActorMessage<?> message, boolean directive) {
		if (message==null)
			throw new NullPointerException();
		
		UUID dest = message.dest();
		
		UUID redirect = system.getRedirector().get(dest);
		if (redirect!=null) 
			dest = redirect;
		
		if (redirect==null) {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecutorService().resource(message.copy());
				return;
			}
			
			if (!dispatch(message, null, directive, true) && !consumerPseudo.apply(message.copy()))
				undelivered(message, message.source(), message.dest());
		}
		else {
			if (system.getResourceCells().containsKey(dest)) {
				system.getExecutorService().resource(message.copy(dest));
				return;
			}
			
			if (!dispatch(message, dest, directive, true) && !consumerPseudo.apply(message.copy(dest)))
				undelivered(message, message.source(), dest);
		}
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
	public void undelivered(ActorMessage<?> message, UUID source, UUID dest) {
		if (system.getConfig().debugUndelivered()) {
			InternalActorCell cell = system.getCells().get(source);
		
			dispatch(message.shallowCopy(dest), system.UNKNOWN_ID(), false, false);
			systemLogger().log(WARN,
				String.format("[UNDELIVERED] Message (%s) from source (%s) - Unavailable actor (%s)",
					message.toString(), cell!=null ? actorLabel(cell.getActor()) : source.toString(), dest
				));
		}
	}

	@Override
	public void unsafe_post(ActorMessage<?> message, UUID source, String alias) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean dispatch(ActorMessage<?> message, UUID dest, boolean directive, boolean debugUndelivered) {
		UUID destination = dest!=null ? dest : message.dest();
		
		ClassicInternalActorCell cell = (ClassicInternalActorCell)system.getCells().get(destination);
		if (cell!=null) {
			if (directive) {
				if (dest!=null)
					cell.directiveQueue().offer(message.copy(dest));
				else
					cell.directiveQueue().offer(message.copy());
			}
			else {
				if (dest!=null)
					cell.outerQueue().offer(message.copy(dest));
				else
					cell.outerQueue().offer(message.copy());
			}
			
			dispatch(cell);
		}
		else if (debugUndelivered)
			undelivered(message, message.source(), destination);
		
		return cell!=null;
	}
	
	protected void dispatch(ClassicInternalActorCell cell) {
		if (cell.aquireAsScheduled()) {
			((ClassicInternalActorExecutorService)system.getExecutorService()).getRunnablePool().submit(cell);
		}
	}
	
	@Override
	public void dispatchFromThread(ClassicInternalActorCell cell) {
		if (cell!=null && cell.hasMessage())
			dispatch(cell);
	}
	
	@Override
	public void postPersistence(ActorMessage<?> message) {
		List<UUID> ids = system.getExecutorService().getPersistenceService().persistenceActorIds();
		int index = ThreadLocalRandom.current().nextInt(ids.size());
		system.getExecutorService().getPersistenceService().getService().send(message.copy(ids.get(index)));
	}
	
	@Deprecated
	@Override
	public void registerCell(InternalActorCell cell) {
		// Not used!
	}
	
	@Deprecated
	@Override
	public void unregisterCell(InternalActorCell cell) {
		// Not used!
	}
	
	@Deprecated
	@Override
	public boolean isRegisteredCell(InternalActorCell cell) {
		// Not used!
		return false;
	}
}
