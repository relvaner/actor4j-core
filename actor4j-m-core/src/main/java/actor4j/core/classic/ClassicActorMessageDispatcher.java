/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.classic;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import actor4j.core.ActorCell;
import actor4j.core.ActorMessageDispatcher;
import actor4j.core.ActorSystemImpl;
import actor4j.core.ActorThread;
import actor4j.core.messages.ActorMessage;
import actor4j.function.BiConsumer;
import actor4j.function.Consumer;

public class ClassicActorMessageDispatcher extends ActorMessageDispatcher {
	protected Map<Long, Queue<UUID>> cellsOnThread; // ThreadID -> List<ActorCellID>
	
	public ClassicActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
		cellsOnThread = new ConcurrentHashMap<>();
		
		biconsumerInnerSingleThreaded = new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> msg) {
				ClassicActorCell cell = (ClassicActorCell)ClassicActorMessageDispatcher.this.system.getCells().get(msg.dest);
				if (cell!=null)
					cell.innerQueue.offer(msg);
			}
		};
		biconsumerInner = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				ClassicActorCell cell = (ClassicActorCell)ClassicActorMessageDispatcher.this.system.getCells().get(msg.dest);
				if (cell!=null)
					cell.innerQueue.offer(msg);
			}
		};
		biconsumerOuter = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				ClassicActorCell cell = (ClassicActorCell)ClassicActorMessageDispatcher.this.system.getCells().get(msg.dest);
				if (cell!=null)
					cell.outerQueueL2.offer(msg);
			}
		};
		biconsumerServer = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				ClassicActorCell cell = (ClassicActorCell)ClassicActorMessageDispatcher.this.system.getCells().get(msg.dest);
				if (cell!=null)
					cell.serverQueueL2.offer(msg);
			}
		};
		biconsumerDirective = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				ClassicActorCell cell = (ClassicActorCell)ClassicActorMessageDispatcher.this.system.getCells().get(msg.dest);
				if (cell!=null)
					cell.directiveQueue.offer(msg);
			}
		};
	}
	
	@Override
	public void beforeRun(List<ActorThread> actorThreads) {
		super.beforeRun(actorThreads);
		
		Iterator<Entry<UUID, Long>> iterator = cellsMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<UUID, Long> entry = iterator.next();
		
			Queue<UUID> cells = cellsOnThread.get(entry.getValue());
			if (cells==null) {
				cells = new ConcurrentLinkedQueue<>();
				cells.add(entry.getKey());
				cellsOnThread.put(entry.getValue(), cells);
			}
			else
				cells.add(entry.getKey());
		}
	}
	
	@Override
	public void registerCell(ActorCell cell) {
		super.registerCell(cell);
		
		Long id = cellsMap.get(cell.getId());
		
		Queue<UUID> cells = cellsOnThread.get(id);
		if (cells==null) {
			cells = new ConcurrentLinkedQueue<>();
			cells.add(cell.getId());
			cellsOnThread.put(id, cells);
		}
		else
			cells.add(cell.getId());
	}
	
	@Override
	public void unregisterCell(ActorCell cell) {
		super.unregisterCell(cell);
		
		Long id = cellsMap.get(cell.getId());
		
		Queue<UUID> cells = cellsOnThread.get(id);
		if (cells!=null)
			cells.remove(cell.getId());
	}
}
