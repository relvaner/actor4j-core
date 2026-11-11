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

import java.util.function.Predicate;

import io.actor4j.core.ActorSystemFactory;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorDistributedGroupMember;
import io.actor4j.core.actors.ActorGroupMember;
import io.actor4j.core.actors.PooledActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.runtime.ActorExecutorService;
import io.actor4j.core.runtime.DefaultActorSystemImpl;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.classic.ClassicActorCell;
import io.actor4j.core.runtime.classic.ClassicPodActorCell;

/*
 * Hybrid runtime: actors are bound to threads like in the default runtime, actors chosen by the placement
 * (default: PooledActor) run in the shared pool of the classic runtime. Root cells stay thread-bound.
 */
public class HybridActorSystemImpl extends DefaultActorSystemImpl {
	public static final Predicate<Actor> POOLED_MARKER = (actor) -> actor instanceof PooledActor;
	// all actors except members of thread-bound groups (e.g. watchdog actors)
	public static final Predicate<Actor> POOLED_ALL = (actor) -> !(actor instanceof ActorGroupMember || actor instanceof ActorDistributedGroupMember);

	protected /*quasi final*/ Predicate<Actor> pooled;
	protected boolean creatingRootCells;

	public HybridActorSystemImpl() {
		this(null);
	}

	public HybridActorSystemImpl(ActorSystemConfig config) {
		this(config, POOLED_MARKER);
	}

	public HybridActorSystemImpl(ActorSystemConfig config, Predicate<Actor> pooled) {
		super(config);

		this.pooled = pooled;
		messageDispatcher = new HybridActorMessageDispatcher(this);
	}

	@Override
	public ActorSystemFactory factory() {
		return (config) -> new HybridActorSystemImpl(config, pooled);
	}

	protected boolean isPooled(Actor actor) {
		return !creatingRootCells && pooled!=null && actor!=null && pooled.test(actor);
	}

	@Override
	protected void resetCells() {
		creatingRootCells = true;
		try {
			super.resetCells();
		}
		finally {
			creatingRootCells = false;
		}
	}

	@Override
	protected InternalActorCell createActorCell(Actor actor) {
		return isPooled(actor) ? new ClassicActorCell(this, actor) : super.createActorCell(actor);
	}

	@Override
	protected InternalActorCell createPodActorCell(Actor actor) {
		return isPooled(actor) ? new ClassicPodActorCell(this, actor) : super.createPodActorCell(actor);
	}

	@Override
	protected ActorExecutorService createActorExecutorService() {
		return new HybridActorExecutorService(this);
	}
}
