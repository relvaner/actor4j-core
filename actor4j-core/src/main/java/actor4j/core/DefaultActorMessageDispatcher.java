/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.UUID;

import actor4j.core.ActorMessageDispatcher;
import actor4j.core.ActorSystemImpl;
import actor4j.core.messages.ActorMessage;
import actor4j.core.mono.MonoActorThread;
import actor4j.function.BiConsumer;

public class DefaultActorMessageDispatcher extends ActorMessageDispatcher {
	public DefaultActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
		biconsumerServer = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((MonoActorThread)threadsMap.get(id_dest)).serverQueueL2.offer(msg);
			}
		};
		biconsumerDirective = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((MonoActorThread)threadsMap.get(id_dest)).directiveQueue.offer(msg);
			}
		};
	}
	
	@Override
	public void post(ActorMessage<?> message, UUID source, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.analyzeMode.get())
			system.analyzerThread.getOuterQueue().offer(message.copy());
		
		if (alias!=null) {
			UUID dest = system.aliases.get(alias);
			message.dest = (dest!=null) ? dest : UUID_ALIAS;
		}
		
		if (system.clientMode && !system.cells.containsKey(message.dest)) {
			system.executerService.client(message.copy(), alias);
			return;
		}
		else if (system.resourceCells.containsKey(message.dest)) {
			system.executerService.resource(message.copy());
			return;
		}
			
		if (system.parallelismMin==1 && system.parallelismFactor==1)
			((MonoActorThread)Thread.currentThread()).innerQueue.offer(message.copy());
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(message.dest);
			
			if (id_dest!=null) {
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					((MonoActorThread)threadsMap.get(id_dest)).innerQueue.offer(message.copy());
				else
					((MonoActorThread)threadsMap.get(id_dest)).outerQueueL2.offer(message.copy());
			}
			else 
				consumerPseudo.accept(message.copy());
		}
	}
	
	@Override
	public void postOuter(ActorMessage<?> message) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.analyzeMode.get())
			system.analyzerThread.getOuterQueue().offer(message.copy());
		
		if (system.resourceCells.containsKey(message.dest)) {
			system.executerService.resource(message.copy());
			return;
		}
		
		Long id_dest = cellsMap.get(message.dest);
		if (id_dest!=null)
			((MonoActorThread)threadsMap.get(id_dest)).outerQueueL2.offer(message.copy());
		else 
			consumerPseudo.accept(message.copy());
	}
}
