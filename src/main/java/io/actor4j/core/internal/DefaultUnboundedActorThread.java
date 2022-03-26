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
package io.actor4j.core.internal;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class DefaultUnboundedActorThread extends DefaultActorThread {
	public DefaultUnboundedActorThread(ThreadGroup group, String name, InternalActorSystem system) {
		super(group, name, system);
	}

	@Override
	public void configQueues() {
		directiveQueue = new ConcurrentLinkedQueue<>(); /* unbounded */
		priorityQueue  = new PriorityBlockingQueue<>(system.getConfig().queueSize()); /* unbounded */
		
		serverQueueL2  = new ConcurrentLinkedQueue<>(); /* unbounded */
		serverQueueL1  = new ArrayDeque<>(system.getConfig().bufferQueueSize()); /* unbounded */
		
		outerQueueL2   = new ConcurrentLinkedQueue<>(); /* unbounded */
		outerQueueL1   = new ArrayDeque<>(system.getConfig().bufferQueueSize()); /* unbounded */
		
		innerQueue     = new LinkedList<>(); /* unbounded */
	}
}
