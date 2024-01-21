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

import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.config.XActorServiceConfig;
import io.actor4j.core.config.XActorSystemConfig;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.extended.XActorSystemImpl;

public interface XActorService extends XActorSystem {
	public static XActorService create() {
		return create(XActorSystemConfig.builder().build());
	}
	
	public static XActorService create(String name) {
		return create(XActorSystemConfig.builder().name(name).build());
	}
	
	public static XActorService create(XActorSystemConfig config) {
		return new XActorSystemImpl(config);
	}
	
	@Deprecated
	@Override
	public default boolean setConfig(ActorSystemConfig config) {
		return false;
	}
	
	@Deprecated
	@Override
	public default boolean setConfig(XActorSystemConfig config) {
		return false;
	}
	
	public boolean setConfig(XActorServiceConfig config);
	
	public boolean hasActor(String uuid);
	
	public boolean sendViaAliasAsServer(ActorMessage<?> message, String alias);	
	public void sendAsServer(ActorMessage<?> message);
}
