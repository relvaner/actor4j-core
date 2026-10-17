/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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
package io.actor4j.core.jctools;

import java.util.Queue;

import org.jctools.queues.MpscArrayQueue;
import org.jctools.queues.MpscLinkedQueue;
import org.jctools.queues.unpadded.MpscLinkedUnpaddedQueue;

import io.actor4j.core.runtime.factory.MessageQueueFactory;

public class JCToolsMessageQueueFactory implements MessageQueueFactory {
	@Override
	public <T> Queue<T> createMpscLinkedQueue() {
		return new MpscLinkedQueue<T>();
	}
	
	@Override
	public <T> Queue<T> createMpscArrayQueue(int capacity) {
		return new MpscArrayQueue<T>(capacity);
	}

	@Override
	public <T> Queue<T> createMpscLinkedUnpaddedQueue() {
		return new MpscLinkedUnpaddedQueue<T>();
	}
}
