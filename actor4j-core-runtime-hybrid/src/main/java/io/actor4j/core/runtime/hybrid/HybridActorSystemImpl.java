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
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.config.HybridActorSystemConfig;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.runtime.ActorExecutorService;
import io.actor4j.core.runtime.DefaultActorSystemImpl;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.classic.ClassicActorCell;
import io.actor4j.core.runtime.classic.ClassicPodActorCell;

/*
 * Hybrid runtime: actors are bound to threads like in the default runtime, actors chosen by the placement
 * (HybridActorSystemConfig.pooledPlacement, default: PooledActor) run in the shared pool of the classic runtime.
 * Root cells stay thread-bound (see generateCell).
 */
public class HybridActorSystemImpl extends DefaultActorSystemImpl {
	protected final Predicate<Actor> explicitPooled; // overrides the placement of the config
	protected /*quasi final*/ Predicate<Actor> pooled;

	public HybridActorSystemImpl() {
		this(null);
	}

	public HybridActorSystemImpl(ActorSystemConfig config) {
		this(config, null);
	}

	// pooled: placement, null = HybridActorSystemConfig.pooledPlacement (default POOLED_MARKER)
	public HybridActorSystemImpl(ActorSystemConfig config, Predicate<Actor> pooled) {
		super(config);

		this.explicitPooled = pooled;
		this.pooled = pooled!=null ? pooled : HybridActorSystemConfig.pooledPlacement(getConfig());
		messageDispatcher = new HybridActorMessageDispatcher(this);
	}

	@Override
	public ActorSystemFactory factory() {
		return (config) -> new HybridActorSystemImpl(config, explicitPooled);
	}

	// only actors added by the user pass here (addActor, addChild); root cells are created directly and stay thread-bound
	@Override
	public InternalActorCell generateCell(Actor actor) {
		if (!(actor instanceof ResourceActor) && pooled!=null && pooled.test(actor))
			return actor instanceof PodActor ? new ClassicPodActorCell(this, actor) : new ClassicActorCell(this, actor);
		return super.generateCell(actor);
	}

	@Override
	protected ActorExecutorService createActorExecutorService() {
		return new HybridActorExecutorService(this);
	}
}
