/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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

import java.util.List;
import java.util.UUID;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.config.XActorSystemConfig;
import io.actor4j.core.exceptions.ActorInitializationException;
import io.actor4j.core.runtime.extended.XDefaultActorSystemImpl;

public interface XActorSystem extends ActorSystem {
	public static XActorSystem create() {
		return create(XActorSystemConfig.builder().build());
	}
	
	public static XActorSystem create(String name) {
		return create(XActorSystemConfig.builder().name(name).build());
	}
	
	public static XActorSystem create(XActorSystemConfig config) {
		return new XDefaultActorSystemImpl(config);
	}
	
	@Deprecated
	@Override
	public default boolean setConfig(ActorSystemConfig config) {
		return false;
	}
	
	public boolean setConfig(XActorSystemConfig config);
	
	public List<UUID> addActor(int instances, Class<? extends Actor> clazz, Object... args) throws ActorInitializationException;
	public UUID addActor(Class<? extends Actor> clazz, Object... args) throws ActorInitializationException;
}
