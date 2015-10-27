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

public class ActorMessageDispatcher {
	protected ActorSystemImpl system;
	
	protected Map<UUID, Long> cellsMap;  // ActorCellID -> ThreadID
	protected Map<Long, ActorThread> threadsMap;
	
	protected Map<UUID, Long> groupsMap; // GroupID -> ThreadID
	
	protected final UUID UUID_ALIAS = UUID_ZERO;
	
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
		
		biconsumerOuter = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				threadsMap.get(id_dest).outerQueueL2.offer(msg);
			}
		};
		biconsumerServer = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				threadsMap.get(id_dest).serverQueueL2.offer(msg);
			}
		};
		biconsumerDirective = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				threadsMap.get(id_dest).directiveQueue.offer(msg);
			}
		};
		
		consumerPseudo = new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> msg) {
				ActorCell cell = ActorMessageDispatcher.this.system.pseudoCells.get(msg.dest);
				if (cell!=null)
					((PseudoActorCell)cell).outerQueueL2.offer(msg);
			}
		};
	}
	
	public void post(ActorMessage<?> message, UUID source) {
		post(message, source, null);
	}
	
	public void post(ActorMessage<?> message, UUID source, String alias) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.analyzeMode.get())
			system.analyzerThread.outerQueueL2.offer(message.copy());
		
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
			((ActorThread)Thread.currentThread()).innerQueue.offer(message.copy());
		else {
			Long id_source = cellsMap.get(source);
			Long id_dest   = cellsMap.get(message.dest);
		
			if (id_dest!=null) {
				if (id_source!=null && id_source.equals(id_dest)
						&& Thread.currentThread().getId()==id_source.longValue())
					threadsMap.get(id_dest).innerQueue.offer(message.copy());
				else
					threadsMap.get(id_dest).outerQueueL2.offer(message.copy());
			}
			else 
				consumerPseudo.accept(message.copy());
		}
	}
	
	public void postQueue(ActorMessage<?> message, BiConsumer<Long, ActorMessage<?>> biconsumer) {
		if (message==null)
			throw new NullPointerException();
		
		if (system.analyzeMode.get())
			system.analyzerThread.outerQueueL2.offer(message.copy());
		
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
