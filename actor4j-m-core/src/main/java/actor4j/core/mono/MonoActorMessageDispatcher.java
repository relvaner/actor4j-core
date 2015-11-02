/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.mono;

import java.util.UUID;

import actor4j.core.ActorCell;
import actor4j.core.ActorMessageDispatcher;
import actor4j.core.ActorSystemImpl;
import actor4j.core.PseudoActorCell;
import actor4j.core.messages.ActorMessage;
import actor4j.function.BiConsumer;
import actor4j.function.Consumer;

public class MonoActorMessageDispatcher extends ActorMessageDispatcher {
	protected BiConsumer<Long, ActorMessage<?>> biconsumerOuter;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerServer;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerDirective;
	
	protected Consumer<ActorMessage<?>> consumerPseudo;
	
	public MonoActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
		biconsumerOuter = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((MonoActorThread)threadsMap.get(id_dest)).outerQueueL2.offer(msg);
			}
		};
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
		
		consumerPseudo = new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> msg) {
				ActorCell cell = MonoActorMessageDispatcher.this.system.getPseudoCells().get(msg.dest);
				if (cell!=null)
					((PseudoActorCell)cell).getOuterQueue().offer(msg);
			}
		};
	}
	
	@Override
	public void post(ActorMessage<?> message, UUID source) {
		post(message, source, null);
	}
	
	@Override
	public void post(ActorMessage<?> message, UUID source, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.getAnalyzeMode().get())
			system.getAnalyzerThread().getOuterQueue().offer(message.copy());
		
		if (alias!=null) {
			UUID dest = system.getAliases().get(alias);
			message.dest = (dest!=null) ? dest : UUID_ALIAS;
		}
		
		if (system.isClientMode() && !system.getCells().containsKey(message.dest)) {
			system.getExecuterService().client(message.copy(), alias);
			return;
		}
		else if (system.getResourceCells().containsKey(message.dest)) {
			system.getExecuterService().resource(message.copy());
			return;
		}
			
		if (system.getParallelismMin()==1 && system.getParallelismFactor()==1)
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
	
	public void postQueue(ActorMessage<?> message, BiConsumer<Long, ActorMessage<?>> biconsumer) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.getAnalyzeMode().get())
			system.getAnalyzerThread().getOuterQueue().offer(message.copy());
		
		if (system.getResourceCells().containsKey(message.dest)) {
			system.getExecuterService().resource(message.copy());
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
		postQueue(message, biconsumerOuter);
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
