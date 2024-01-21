/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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
package io.actor4j.core.actors;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.actor4j.core.messages.ActorMessage;

public interface PseudoActorRef {
	public String getName();
	public UUID getId();
	public UUID self();
	
	public boolean run();
	public boolean runAll();
	public boolean runOnce();
	public Stream<ActorMessage<?>> stream();
	
	public ActorMessage<?> await();
	public ActorMessage<?> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
	public <T> T await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, T> action, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
	
	public void send(ActorMessage<?> message);
	public void sendViaPath(ActorMessage<?> message, String path);
	public void sendViaAlias(ActorMessage<?> message, String alias);
	public void send(ActorMessage<?> message, UUID dest);
	public <T> void tell(T value, int tag, UUID dest);
	public <T> void tell(T value, int tag, String alias);
	public void forward(ActorMessage<?> message, UUID dest);
	public void setAlias(String alias);
	
	public void reset();
}
