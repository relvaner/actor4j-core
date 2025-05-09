/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
package io.actor4j.core;

import java.util.function.Predicate;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public interface EmbeddedActorCell {
	public ActorRef host();
	
	public ActorId getId();
	public ActorId getParent();
	
	public void become(Predicate<ActorMessage<?>> behaviour, boolean replace);
	public void unbecome();
	public void unbecomeAll();
	
	public void send(ActorMessage<?> message);
	
	public void preStart();
	public void restart(Exception reason);
	public void stop();
}
