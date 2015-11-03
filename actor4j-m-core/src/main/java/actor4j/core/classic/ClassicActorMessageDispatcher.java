/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.classic;

import actor4j.core.ActorMessageDispatcher;
import actor4j.core.ActorSystemImpl;
import actor4j.core.messages.ActorMessage;
import actor4j.function.BiConsumer;
import actor4j.function.Consumer;

public class ClassicActorMessageDispatcher extends ActorMessageDispatcher {
	public ClassicActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
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
}
