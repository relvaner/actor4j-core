/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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

import io.actor4j.core.config.ActorServiceConfig;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.messages.ActorMessage;

public interface ActorService extends ActorSystem {
	public static ActorSystem create(ActorSystemFactory factory, String name) {
		return create(factory, ActorServiceConfig.builder().name(name).build());
	}
	
	public static ActorService create(ActorSystemFactory factory) {
		return create(factory, ActorServiceConfig.create());
	}
	
	public static ActorService create(ActorSystemFactory factory, ActorServiceConfig config) {
		return (ActorService)factory.apply(config!=null ? config : ActorServiceConfig.create());
	}	
	
	@Deprecated
	@Override
	public default boolean setConfig(ActorSystemConfig config) {
		return false;
	}
	
	public boolean setConfig(ActorServiceConfig config);

	public boolean hasActor(String id);
	
	public boolean sendViaAliasAsServer(ActorMessage<?> message, String alias);	
	public void sendAsServer(ActorMessage<?> message);
	
	public boolean isShutdownHookTriggered();
	public void shutdownHookTriggered();
}
