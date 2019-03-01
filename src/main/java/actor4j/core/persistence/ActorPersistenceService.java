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
package actor4j.core.persistence;

import java.util.UUID;

import actor4j.core.ActorService;
import actor4j.core.ActorSystem;
import actor4j.core.persistence.actor.PersistenceServiceActor;
import actor4j.core.persistence.connectors.PersistenceConnector;

public class ActorPersistenceService {
	protected ActorService service;
	protected PersistenceConnector connector;
	
	public ActorPersistenceService(ActorSystem parent, int parallelismMin, int parallelismFactor, PersistenceConnector connector) {
		super();
		
		this.connector = connector;

		service = new ActorService("actor4j-persistence");
		service.setParallelismMin(parallelismMin);
		service.setParallelismFactor(parallelismFactor);
		
		connector.open();
		for (int i=0; i<parallelismMin*parallelismFactor; i++) {
			String alias = getAlias(i);
			UUID id = service.addActor(() -> new PersistenceServiceActor(alias, connector.createAdapter(parent)));
			service.setAlias(id, alias);
		}
	}
	
	public static String getAlias(int index) {
		return "persistence-actor-"+String.valueOf(index);
	}
	
	public ActorService getService() {
		return service;
	}

	public void start() {
		service.start();
	}
	
	public void shutdown() {
		service.shutdownWithActors(true);
		connector.close();
	}
}
