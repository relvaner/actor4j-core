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
package io.actor4j.core.runtime.classic;

import static io.actor4j.core.logging.ActorLogger.*;

import java.util.concurrent.atomic.AtomicReference;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.InternalPodActorCell;

public class ClassicPodActorCell extends ClassicActorCell implements InternalPodActorCell {
	protected final AtomicReference<PodContext> contextReference;
	
	public ClassicPodActorCell(InternalActorSystem system, Actor actor) {
		super(system, actor);
		
		this.contextReference = new AtomicReference<>();
	}
	
	@Override
	public PodContext getContext() {
		return contextReference.get();
	}
	
	@Override
	public void setContext(PodContext context) {
		contextReference.set(context);
	}
	
	@Override
	public void preStart() {
		systemLogger().log(INFO, String.format("[REPLICATION] PodActor (%s, %s) starting", getContext().domain(), getId()));
		super.preStart();
	}
}
