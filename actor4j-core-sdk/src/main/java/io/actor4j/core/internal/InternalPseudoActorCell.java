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
package io.actor4j.core.internal;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.actor4j.core.messages.ActorMessage;

public interface InternalPseudoActorCell extends InternalActorCell {
	public UUID pseudo_addCell(InternalPseudoActorCell cell);
	
	public boolean run();
	public boolean runAll();
	public boolean runOnce();
	public Stream<ActorMessage<?>> stream();
	public ActorMessage<?> await();
	public ActorMessage<?> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
	public <T> T await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, T> action, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
	
	public Queue<ActorMessage<?>> getOuterQueue();
	
	public void reset();
}
