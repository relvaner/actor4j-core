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
package io.actor4j.core.runtime.hybrid;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.DefaultActorMessageDispatcher;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.classic.ActorMessageDispatcherCallback;
import io.actor4j.core.runtime.classic.ClassicDefaultActorMessageDispatcher;
import io.actor4j.core.runtime.classic.ClassicInternalActorCell;

// routes messages for pooled (classic) cells to the classic dispatcher, all others as in the default runtime
public class HybridActorMessageDispatcher extends DefaultActorMessageDispatcher implements ActorMessageDispatcherCallback {
	protected final ClassicDefaultActorMessageDispatcher classic;

	public HybridActorMessageDispatcher(InternalActorSystem system) {
		super(system);

		classic = new ClassicDefaultActorMessageDispatcher(system) {
			@Override
			public void undelivered(ActorMessage<?> message, ActorId source, ActorId dest) {
				HybridActorMessageDispatcher.this.undelivered(message, source, dest); // UNKNOWN is thread-bound
			}
		};
	}

	protected ActorId resolve(ActorMessage<?> message, String alias) {
		ActorId dest = message.dest();
		if (alias!=null) {
			dest = system.internal_resolveAlias(alias);
			if (dest==null)
				dest = system.ALIAS_ID();
		}
		ActorId redirect = dest!=null ? dest.redirectId() : null;

		return redirect!=null ? redirect : dest;
	}

	protected boolean isPooled(ActorMessage<?> message, String alias) {
		return message!=null && resolve(message, alias) instanceof ClassicInternalActorCell;
	}

	@Override
	public void post(ActorMessage<?> message, ActorId source, String alias) {
		if (isPooled(message, alias))
			classic.post(message, source, alias);
		else
			super.post(message, source, alias);
	}

	@Override
	public void unsafe_post(ActorMessage<?> message, ActorId source, String alias) {
		if (isPooled(message, alias))
			classic.post(message, source, alias); // no thread affinity
		else
			super.unsafe_post(message, source, alias);
	}

	@Override
	public void postOuter(ActorMessage<?> message) {
		if (isPooled(message, null))
			classic.postOuter(message);
		else
			super.postOuter(message);
	}

	@Override
	public void postServer(ActorMessage<?> message) {
		if (isPooled(message, null))
			classic.postServer(message);
		else
			super.postServer(message);
	}

	@Override
	public void postPriority(ActorMessage<?> message) {
		if (isPooled(message, null))
			classic.postPriority(message);
		else
			super.postPriority(message);
	}

	@Override
	public void postDirective(ActorMessage<?> message) {
		if (isPooled(message, null))
			classic.postDirective(message);
		else
			super.postDirective(message);
	}

	@Override
	public void dispatchFromThread(ClassicInternalActorCell cell) {
		classic.dispatchFromThread(cell);
	}
}
