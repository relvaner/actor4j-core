/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime;

import static io.actor4j.core.utils.ActorUtils.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;

public class PseudoActorCell extends DefaultActorCell implements InternalPseudoActorCell {
	protected final Queue<ActorMessage<?>> outerQueueL2;
	protected final Queue<ActorMessage<?>> outerQueueL1;
	
	public PseudoActorCell(InternalActorSystem system, Actor actor, boolean blocking) {
		super(system, actor);
		
		if (blocking)
			outerQueueL2 = new LinkedBlockingQueue<>();
		else
			outerQueueL2 = new ConcurrentLinkedQueue<>();
		
		outerQueueL1 = new LinkedList<>();
	}
	
	@Override
	public void internal_receive(ActorMessage<?> message) {
		Consumer<ActorMessage<?>> behaviour = behaviourStack.peek();
		if (behaviour==null)
			actor.receive(message);
		else
			behaviour.accept(message);	
	}
	
	@Override
	public UUID pseudo_addCell(InternalPseudoActorCell cell) {
		return system.pseudo_addCell(cell);
	}
	
	@Override
	public void send(ActorMessage<?> message) {
		system.send(message);
	}
	
	@Override
	public void send(ActorMessage<?> message, String alias) {
		if (alias!=null) {
			List<UUID> destinations = system.getActorsFromAlias(alias);

			UUID dest = null;
			if (!destinations.isEmpty()) {
				if (destinations.size()==1)
					dest = destinations.get(0);
				else {
					Random random = new Random();
					dest = destinations.get(random.nextInt(destinations.size()));
				}
			}
			message = message.shallowCopy((dest!=null) ? dest : UUID_ZERO);
		}
		
		system.send(message);
	}
	
	@Deprecated
	@Override
	public void unsafe_send(ActorMessage<?> message) {
		// empty
	}
	
	@Deprecated
	@Override
	public void unsafe_send(ActorMessage<?> message, String alias) {
		// empty
	}
	
	@Deprecated
	@Override
	public UUID internal_addChild(InternalActorCell cell) {
		return null;
	}
	
	@Deprecated
	@Override
	public UUID addChild(ActorFactory factory) {
		return null;
	}
	
	@Deprecated
	@Override
	public List<UUID> addChild(ActorFactory factory, int instances) {
		return null;
	}
	
	@Override
	public void restart(Exception reason) {
		postStop();
		postRestart(reason);
	}
	
	@Override
	public void stop() {
		internal_stop();
	}
	
	protected void failsafeMethod(ActorMessage<?> message) {
		try {
			internal_receive(message);
		}
		catch(Exception e) {
			system.getExecuterService().getFailsafeManager().notifyErrorHandler(e, "pseudo", id);
			system.getActorStrategyOnFailure().handle(this, e);
		}	
	}
	
	protected boolean poll(Queue<ActorMessage<?>> queue) {
		boolean result = false;
		
		ActorMessage<?> message = queue.poll();
		if (message!=null) {
			failsafeMethod(message);
			result = true;
		} 
		
		return result;
	}
	
	public boolean run() {
		boolean result = false;
		
		boolean hasNextOuter = outerQueueL1.peek()!=null;
		if (!hasNextOuter && outerQueueL2.peek()!=null) {
			ActorMessage<?> message = null;
			for (int j=0; (message=outerQueueL2.poll())!=null && j<system.getConfig().bufferQueueSize(); j++)
				outerQueueL1.offer(message);
		}
		while (poll(outerQueueL1))
			result = true;
		
		return result;
	}
	
	public boolean runAll() {
		boolean result = false;
		
		boolean hasNextOuter = outerQueueL1.peek()!=null;
		if (!hasNextOuter && outerQueueL2.peek()!=null) {
			ActorMessage<?> message = null;
			while ((message=outerQueueL2.poll())!=null)
				outerQueueL1.offer(message);
		}
		while (poll(outerQueueL1))
			result = true;
		
		return result;
	}
	
	public boolean runOnce() {
		boolean hasNextOuter = outerQueueL1.peek()!=null;
		if (!hasNextOuter && outerQueueL2.peek()!=null) {
			ActorMessage<?> message = null;
			for (int j=0; (message=outerQueueL2.poll())!=null && j<system.getConfig().bufferQueueSize(); j++)
				outerQueueL1.offer(message);
		}
		return poll(outerQueueL1);
	}
		
	public Stream<ActorMessage<?>> stream() {
		boolean hasNextOuter = outerQueueL1.peek()!=null;
		if (!hasNextOuter && outerQueueL2.peek()!=null) {
			ActorMessage<?> message = null;
			for (int j=0; (message=outerQueueL2.poll())!=null && j<system.getConfig().bufferQueueSize(); j++)
				outerQueueL1.offer(message);
		}
		
		return outerQueueL1.stream();
	}
	
	public ActorMessage<?> await() {
		ActorMessage<?> result = null;
		
		if (outerQueueL2 instanceof BlockingQueue)
			try {
				result = ((BlockingQueue<ActorMessage<?>>)outerQueueL2).take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		
		return result;
	}
	
	public ActorMessage<?> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		ActorMessage<?> result = null;
		
		if (outerQueueL2 instanceof BlockingQueue) {
			result = ((BlockingQueue<ActorMessage<?>>)outerQueueL2).poll(timeout, unit);
			
			if (result==null)
				throw new TimeoutException();
		}
		
		return result;
	}
	
	public <T> T await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, T> action, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		T result = null;
		
		if (outerQueueL2 instanceof BlockingQueue) {
			timeout = unit.toMillis(timeout);
			unit = TimeUnit.MILLISECONDS;
			long start = System.currentTimeMillis();
			long duration = 0;
			
			ActorMessage<?> message = ((BlockingQueue<ActorMessage<?>>)outerQueueL2).poll(timeout, unit);
			while ((message!=null && !predicate.test(message)) && ((duration=(System.currentTimeMillis()-start))<timeout) && !Thread.currentThread().isInterrupted())
				message = ((BlockingQueue<ActorMessage<?>>)outerQueueL2).poll(timeout-duration, unit);
			
			if (message!=null && predicate.test(message))
				result = action.apply(message);
			else if (message==null)
				throw new TimeoutException();
		}
			
		return result;
	}
	
	@Override
	public Queue<ActorMessage<?>> getOuterQueue() {
		return outerQueueL2;
	}

	public Queue<ActorMessage<?>> getOuterQueueL2() {
		return outerQueueL2;
	}
	
	public Queue<ActorMessage<?>> getOuterQueueL1() {
		return outerQueueL1;
	}
	
	public void reset() {
		outerQueueL2.clear();
		outerQueueL1.clear();
	}
}
