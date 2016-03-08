/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.UUID;

import actor4j.core.ActorMessageDispatcher;
import actor4j.core.ActorSystemImpl;
import actor4j.core.messages.ActorMessage;
import actor4j.function.BiConsumer;
import actor4j.function.Consumer;

public class DefaultActorMessageDispatcher extends ActorMessageDispatcher {
	protected BiConsumer<Long, ActorMessage<?>> biconsumerServer;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerDirective;
	
	protected Consumer<ActorMessage<?>> consumerPseudo;
	
	public DefaultActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
		biconsumerServer = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((DefaultActorThread)threadsMap.get(id_dest)).serverQueueL2.offer(msg);
			}
		};
		biconsumerDirective = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((DefaultActorThread)threadsMap.get(id_dest)).directiveQueue.offer(msg);
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
	}
	
	@Override
	public void post(ActorMessage<?> message, UUID source, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		if (alias!=null) {
			UUID dest = system.aliases.get(alias);
			message.dest = (dest!=null) ? dest : UUID_ALIAS;
		}
		
		if (system.pseudoCells.containsKey(message.dest)) {
			consumerPseudo.accept(message.copy());
			return;
		}
		else if (system.clientMode && !system.cells.containsKey(message.dest)) {
			system.executerService.client(message.copy(), alias);
			return;
		}
		else if (system.resourceCells.containsKey(message.dest)) {
			system.executerService.resource(message.copy());
			return;
		}
		
		if (system.parallelismMin==1 && system.parallelismFactor==1)
			((DefaultActorThread)Thread.currentThread()).innerQueue.offer(message.copy());
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(message.dest);
			
			if (id_dest!=null) {
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					((DefaultActorThread)threadsMap.get(id_dest)).innerQueue.offer(message.copy());
				else
					((DefaultActorThread)threadsMap.get(id_dest)).outerQueueL2.offer(message.copy());
			}	
		}
	}
	
	protected void postQueue(ActorMessage<?> message, BiConsumer<Long, ActorMessage<?>> biconsumer) {
		if (message==null)
			throw new NullPointerException();
		
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
		
		if (system.resourceCells.containsKey(message.dest)) {
			system.executerService.resource(message.copy());
			return;
		}
		
		Long id_dest = cellsMap.get(message.dest);
		if (id_dest!=null)
			((DefaultActorThread)threadsMap.get(id_dest)).outerQueueL2.offer(message.copy());
		else 
			consumerPseudo.accept(message.copy());
	}
	
	@Override
	public void postServer(ActorMessage<?> message) {
		postQueue(message, biconsumerServer);
	}
	
	@Override
	public void postDirective(ActorMessage<?> message) {
		postQueue(message, biconsumerDirective);
	}
}
