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
package io.actor4j.core.runtime.extended.utils;

import io.actor4j.core.messages.ActorMessage;

//under overload messages of the same sender may overtake each other (fallback queue is drained after L2)
public interface AntiFloodingHandler {
	public void handleQueue(ActorMessage<?> message, boolean overloadDetected /*e.g. 80% capacity of exposedQueue reached*/);
	
	public long getDroppedMessages();
	
	public static boolean isDirective(ActorMessage<?> message) {
		return message.tag() < 0;
	}
}
