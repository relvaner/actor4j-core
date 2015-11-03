/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import static actor4j.core.utils.ActorUtils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import actor4j.core.messages.ActorMessage;
import actor4j.function.BiConsumer;
import actor4j.function.Consumer;

public abstract class ActorMessageDispatcher {
	protected ActorSystemImpl system;
	
	protected Map<UUID, Long> cellsMap;  // ActorCellID -> ThreadID
	protected Map<Long, ActorThread> threadsMap;
	
	protected Map<UUID, Long> groupsMap; // GroupID -> ThreadID
	
	protected final UUID UUID_ALIAS = UUID_ZERO;
	
	protected Consumer<ActorMessage<?>> biconsumerInnerSingleThreaded;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerInner;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerOuter;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerServer;
	protected BiConsumer<Long, ActorMessage<?>> biconsumerDirective;
	
	protected Consumer<ActorMessage<?>> consumerPseudo;
	
	public ActorMessageDispatcher(ActorSystemImpl system) {
		super();
		
		this.system = system;
		
		cellsMap = new ConcurrentHashMap<>();
		threadsMap = new HashMap<>();
		
		groupsMap = new ConcurrentHashMap<>();
		
		consumerPseudo = new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> msg) {
				ActorCell cell = ActorMessageDispatcher.this.system.getPseudoCells().get(msg.dest);
				if (cell!=null)
					((PseudoActorCell)cell).getOuterQueue().offer(msg);
			}
		};
	}
	
	public void post(ActorMessage<?> message, UUID source) {
		post(message, source, null);
	}
	
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
			biconsumerInnerSingleThreaded.accept(message.copy());
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(message.dest);
		
			if (id_dest!=null) {
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					biconsumerInner.accept(id_dest, message.copy());
				else
					biconsumerOuter.accept(id_dest, message.copy());
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
	
	public void postOuter(ActorMessage<?> message) {
		postQueue(message, biconsumerOuter);
	}
	
	public void postServer(ActorMessage<?> message) {
		postQueue(message, biconsumerServer);
	}
	
	public void postDirective(ActorMessage<?> message) {
		postQueue(message, biconsumerDirective);
	}
	
	public void beforeRun(List<ActorThread> actorThreads) {
		system.actorBalancingOnCreation.balance(cellsMap, actorThreads, groupsMap, system.cells);
		
		for(ActorThread t : actorThreads)
			threadsMap.put(t.getId(), t);
	}
	
	public void registerCell(ActorCell cell) {
		system.actorBalancingOnRuntime.registerCell(cellsMap, threadsMap, groupsMap, cell);
	}
	
	public void unregisterCell(ActorCell cell) {
		system.actorBalancingOnRuntime.unregisterCell(cellsMap, threadsMap, groupsMap, cell);
	}
}
