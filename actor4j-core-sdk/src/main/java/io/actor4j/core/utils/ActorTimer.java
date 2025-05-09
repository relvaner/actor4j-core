/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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
package io.actor4j.core.utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public interface ActorTimer {
	public ScheduledFuture<?> scheduleOnce(final Supplier<ActorMessage<?>> supplier, final ActorId dest, long delay, TimeUnit unit);
	public ScheduledFuture<?> scheduleOnce(final ActorMessage<?> message, final ActorId dest, long delay, TimeUnit unit);
	public ScheduledFuture<?> scheduleOnce(final Supplier<ActorMessage<?>> supplier, final String alias, long delay, TimeUnit unit);
	public ScheduledFuture<?> scheduleOnce(final ActorMessage<?> message, final String alias, long delay, TimeUnit unit);
	public ScheduledFuture<?> scheduleOnce(final Supplier<ActorMessage<?>> supplier, final ActorGroup group, long delay, TimeUnit unit);
	public ScheduledFuture<?> scheduleOnce(final ActorMessage<?> message, final ActorGroup group, long delay, TimeUnit unit);
	public ScheduledFuture<?> schedule(final Supplier<ActorMessage<?>> supplier, final ActorId dest, long initalDelay, long period, TimeUnit unit);
	public ScheduledFuture<?> schedule(final ActorMessage<?> message, final ActorId dest, long initalDelay, long period, TimeUnit unit);
	public ScheduledFuture<?> schedule(final Supplier<ActorMessage<?>> supplier, final String alias, long initalDelay, long period, TimeUnit unit);
	public ScheduledFuture<?> schedule(final ActorMessage<?> message, final String alias, long initalDelay, long period, TimeUnit unit);
	public ScheduledFuture<?> schedule(final Supplier<ActorMessage<?>> supplier, final ActorGroup group, long initalDelay, long period, TimeUnit unit);
	public ScheduledFuture<?> schedule(final ActorMessage<?> message, final ActorGroup group, long initalDelay, long period, TimeUnit unit);
}
