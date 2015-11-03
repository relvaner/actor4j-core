/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.mono;

import actor4j.core.ActorMessageDispatcher;
import actor4j.core.ActorSystemImpl;
import actor4j.core.messages.ActorMessage;
import actor4j.function.BiConsumer;
import actor4j.function.Consumer;

public class MonoActorMessageDispatcher extends ActorMessageDispatcher {
	public MonoActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
		
		biconsumerInnerSingleThreaded = new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> msg) {
				((MonoActorThread)Thread.currentThread()).innerQueue.offer(msg);
			}
		};
		biconsumerInner = new BiConsumer<Long, ActorMessage<?>>() {
			@Override
			public void accept(Long id_dest, ActorMessage<?> msg) {
				((MonoActorThread)threadsMap.get(id_dest)).innerQueue.offer(msg);
			}
		};
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
	}
}
