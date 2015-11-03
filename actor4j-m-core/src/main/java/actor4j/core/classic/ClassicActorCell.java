/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.classic;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.jctools.queues.MpscArrayQueue;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystemImpl;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class ClassicActorCell extends ActorCell {
	protected Queue<ActorMessage<?>> directiveQueue;
	protected Queue<ActorMessage<?>> innerQueue;
	protected Queue<ActorMessage<?>> outerQueueL2;
	protected Queue<ActorMessage<?>> outerQueueL1;
	protected Queue<ActorMessage<?>> serverQueueL2;
	protected Queue<ActorMessage<?>> serverQueueL1;

	public ClassicActorCell(ActorSystemImpl system, Actor actor) {
		super(system, actor);
		
		directiveQueue = new MpscArrayQueue<>(system.getQueueSize());
		serverQueueL2  = new MpscArrayQueue<>(system.getQueueSize());
		serverQueueL1  = new LinkedList<>();
		outerQueueL2   = new MpscArrayQueue<>(system.getQueueSize());
		outerQueueL1   = new LinkedList<>();
		innerQueue     = new CircularFifoQueue<>(system.getQueueSize());
	}
	
	public Queue<ActorMessage<?>> getDirectiveQueue() {
		return directiveQueue;
	}
	
	public Queue<ActorMessage<?>> getServerQueueL2() {
		return serverQueueL2;
	}
	
	public Queue<ActorMessage<?>> getServerQueueL1() {
		return serverQueueL1;
	}
	
	public Queue<ActorMessage<?>> getOuterQueueL2() {
		return outerQueueL2;
	}
	
	public Queue<ActorMessage<?>> getOuterQueueL1() {
		return outerQueueL1;
	}
	
	public Queue<ActorMessage<?>> getInnerQueue() {
		return innerQueue;
	}
}
