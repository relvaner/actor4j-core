/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.persistence;

import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class ActorPersistence {
	@SafeVarargs
	public static <T> void persist(Consumer<T> handler, T... events) {
		persist("actor4j-persistence-persist", handler, events);
	}
	
	public static <T> void saveSnapshot(Consumer<T> handler, T state) {
		persist("actor4j-persistence-snapshot", handler, state);
	}
	
	@SafeVarargs
	protected static <T> void persist(String persistenceUnitName, Consumer<T> handler, T... events) {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnitName);
		EntityManager em = factory.createEntityManager();
		
		em.getTransaction().begin();
		for (Object event : events)
			em.persist(event);
		em.getTransaction().commit();
	    em.close();
	    
	    factory.close();
		
	    if (handler!=null)
	    	for (T event : events)
	    		handler.accept(event);
	}
}
