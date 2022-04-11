/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.persistence;

import java.util.UUID;

import io.actor4j.core.ActorService;
import io.actor4j.core.ActorSystem;
import io.actor4j.core.config.ActorServiceConfig;
import io.actor4j.core.internal.persistence.ActorPersistenceService;
import io.actor4j.core.internal.persistence.actor.PersistenceServiceActor;
import io.actor4j.core.persistence.drivers.PersistenceDriver;

public class DefaultActorPersistenceService implements ActorPersistenceService {
	protected final ActorService service;
	protected final PersistenceDriver driver;
	
	public DefaultActorPersistenceService(ActorSystem parent, int parallelism, int parallelismFactor, PersistenceDriver driver) {
		super();
		
		this.driver = driver;

		ActorServiceConfig config = ActorServiceConfig.builder()
			.name("actor4j-persistence")
			.parallelism(parallelism)
			.parallelismFactor(parallelismFactor)
			.horizontalPodAutoscalerSyncTime(Integer.MAX_VALUE) // TODO: disabled
			.build();
		service = ActorService.create(config);
		
		driver.open();
		for (int i=0; i<parallelism*parallelismFactor; i++) {
			String alias = getAlias(i);
			UUID id = service.addActor(() -> new PersistenceServiceActor(alias, driver.createPersistenceImpl(parent)));
			service.setAlias(id, alias);
		}
	}
	
	@Override
	public String getAlias(int index) {
		return "persistence-actor-"+String.valueOf(index);
	}
	
	@Override
	public ActorService getService() {
		return service;
	}

	@Override
	public void start() {
		service.start();
	}
	
	@Override
	public void shutdown() {
		service.shutdownWithActors(true);
		driver.close();
	}
}
