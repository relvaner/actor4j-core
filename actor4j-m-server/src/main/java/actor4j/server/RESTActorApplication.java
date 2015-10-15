/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server;

import javax.annotation.PreDestroy;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import actor4j.core.ActorSystem;
import static actor4j.core.ActorLogger.*;

public abstract class RESTActorApplication extends ResourceConfig {
	protected ActorSystem system;
	
	public RESTActorApplication() {
		this(null);
	}
	
	public RESTActorApplication(String name) {
		super();
		
		system = new ActorSystem(name);
		configure(system);
		system.setClientRunnable(new RESTActorClientRunnable(system.getServerURIs(), system.getParallelismMin()*system.getParallelismFactor(), 10000));
		system.start();
		
		logger().info(String.format("%s - System started...", system.getName()));

		register(new AbstractBinder() {
			protected void configure() {
				bind(system).to(ActorSystem.class);
			}
		});

		register(new JacksonJsonProvider().configure(SerializationFeature.INDENT_OUTPUT, true));

		packages("actor4j.server");
	}
	
	protected abstract void configure(ActorSystem system);
		
	@PreDestroy
	public void shutdown() {
		system.shutdown(true);
		logger().info(String.format("%s - System stopped...", system.getName()));
	}
}
