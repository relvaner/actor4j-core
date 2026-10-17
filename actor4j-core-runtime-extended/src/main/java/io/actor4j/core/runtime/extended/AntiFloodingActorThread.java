/*
 * Copyright (c) 2015-2025, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.extended;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.extended.utils.AntiFloodingHandler;

public abstract class AntiFloodingActorThread extends XDefaultActorThread {
	protected Queue<ActorMessage<?>> serverQueueFallback;
	protected Queue<ActorMessage<?>> outerQueueFallback;
	protected Queue<ActorMessage<?>> innerQueueFallback;
	
	protected AntiFloodingHandler afHandlerServerQueue;
	protected AntiFloodingHandler afHandlerOuterQueue;
	protected AntiFloodingHandler afHandlerInnerQueue;
	
	protected int overloadThreshold; // 80% of queueSize
	
	public AntiFloodingActorThread(ThreadGroup group, String name, InternalActorSystem system) {
		super(group, name, system);
	}

	@Override
	public void configQueues() {
		directiveQueue = system.getMessageQueueFactory().createMpscLinkedQueue(); /* unbounded */
		priorityQueue  = new PriorityBlockingQueue<>(system.getConfig().queueSize()); /* unbounded */
		
		serverQueueL2  = system.getMessageQueueFactory().createMpscArrayQueue(system.getConfig().queueSize()); /* bounded by afHandlerServerQueue */
		serverQueueL1  = new ArrayDeque<>(system.getConfig().bufferQueueSize()); /* unbounded */
		
		outerQueueL2   = system.getMessageQueueFactory().createMpscArrayQueue(system.getConfig().queueSize()); /* bounded by afHandlerOuterQueue */
		outerQueueL1   = new ArrayDeque<>(system.getConfig().bufferQueueSize()); /* unbounded */
		
		innerQueue = new ArrayDeque<>(system.getConfig().bufferQueueSize()); /* bounded by afHandlerInnerQueue */
		
		serverQueueFallback = system.getMessageQueueFactory().createMpscLinkedQueue(); /* unbounded */
		outerQueueFallback  = system.getMessageQueueFactory().createMpscLinkedQueue(); /* unbounded */
		innerQueueFallback  = new LinkedList<>(); /* unbounded */
		
		overloadThreshold = (int)(system.getConfig().queueSize()*0.8);
	}
	
	@Override
	public void processMessages(MessageCounters ctn) {
		if (serverMode) {
			for (; ctn.hasNextServer<maxThroughput && poll(serverQueueL1); ctn.hasNextServer++);
			if (ctn.hasNextServer<maxThroughput && serverQueueL2.peek()!=null) {
				ActorMessage<?> message = null;
				int delta = bufferQueueSize-serverQueueL1.size();
				for (int j=0; j<delta && (message=serverQueueL2.poll())!=null; j++)
					serverQueueL1.offer(message);
			
				for (; ctn.hasNextServer<maxThroughput && poll(serverQueueL1); ctn.hasNextServer++);
			}
			if (ctn.hasNextServer<maxThroughput && serverQueueFallback.peek()!=null) {
				ActorMessage<?> message = null;
				int delta = bufferQueueSize-serverQueueL1.size();
				for (int j=0; j<delta && (message=serverQueueFallback.poll())!=null; j++)
					serverQueueL1.offer(message);
			
				for (; ctn.hasNextServer<maxThroughput && poll(serverQueueL1); ctn.hasNextServer++);
			}
		}
		
		for (; ctn.hasNextOuter<maxThroughput && poll(outerQueueL1); ctn.hasNextOuter++);
		if (ctn.hasNextOuter<maxThroughput && outerQueueL2.peek()!=null) {
			ActorMessage<?> message = null;
			int delta = bufferQueueSize-outerQueueL1.size();
			for (int j=0; j<delta && (message=outerQueueL2.poll())!=null; j++)
				outerQueueL1.offer(message);

			for (; ctn.hasNextOuter<maxThroughput && poll(outerQueueL1); ctn.hasNextOuter++);
		}
		if (ctn.hasNextOuter<maxThroughput && outerQueueFallback.peek()!=null) {
			ActorMessage<?> message = null;
			int delta = bufferQueueSize-outerQueueL1.size();
			for (int j=0; j<delta && (message=outerQueueFallback.poll())!=null; j++)
				outerQueueL1.offer(message);

			for (; ctn.hasNextOuter<maxThroughput && poll(outerQueueL1); ctn.hasNextOuter++);
		}
		
		for (; ctn.hasNextInner<maxThroughput && poll(innerQueue); ctn.hasNextInner++);
		if (ctn.hasNextInner<maxThroughput && innerQueueFallback.peek()!=null) {
			ActorMessage<?> message = null;
			int delta = bufferQueueSize-innerQueue.size();
			for (int j=0; j<delta && (message=innerQueueFallback.poll())!=null; j++)
				innerQueue.offer(message);

			for (; ctn.hasNextInner<maxThroughput && poll(innerQueue); ctn.hasNextInner++);
		}
	}
	
	@Override
	protected boolean hasPendingMessages() {
		return super.hasPendingMessages() || !outerQueueFallback.isEmpty() || (serverMode && !serverQueueFallback.isEmpty());
	}
	
	@Override
	public void serverQueue(ActorMessage<?> message) {
		afHandlerServerQueue.handleQueue(message, serverQueueL2.size()>overloadThreshold);
	}

	@Override
	public void outerQueue(ActorMessage<?> message) {
		afHandlerOuterQueue.handleQueue(message, outerQueueL2.size()>overloadThreshold);
	}

	@Override
	public void innerQueue(ActorMessage<?> message) {
		afHandlerInnerQueue.handleQueue(message, innerQueue.size()>overloadThreshold);
	}
}
