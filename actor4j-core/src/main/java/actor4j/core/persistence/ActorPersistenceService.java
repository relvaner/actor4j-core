/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.persistence;

import java.util.UUID;

import actor4j.core.ActorService;
import actor4j.core.ActorSystem;
import actor4j.core.persistence.actor.PersistenceServiceActor;

public class ActorPersistenceService {
	protected ActorSystem parent;
	protected ActorService service;
	
	protected String host;
	protected int port;
	protected String databaseName;
	
	public ActorPersistenceService(ActorSystem parent, int parallelismMin, int parallelismFactor, String host, int port, String databaseName) {
		super();
		
		service = new ActorService("actor4j-persistence-service");
		service.setParallelismMin(parallelismMin);
		service.setParallelismFactor(parallelismFactor);
		
		for (int i=0; i<parallelismMin*parallelismFactor; i++) {
			String alias = getAlias(i);
			UUID id = service.addActor(() -> new PersistenceServiceActor(parent, alias, host, port, databaseName));
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
		service.shutdownWithActors();
	}
	
	public void shutdown(boolean await) {
		service.shutdownWithActors(await);
	}
}
