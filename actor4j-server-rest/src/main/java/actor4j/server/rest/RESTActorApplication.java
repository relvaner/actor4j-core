/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.rest;

import static actor4j.core.utils.ActorLogger.*;

import javax.annotation.PreDestroy;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import actor4j.core.ActorService;

public abstract class RESTActorApplication extends ResourceConfig {
	protected static ActorService service;
	
	public RESTActorApplication() {
		this(null);
	}
	
	public static ActorService getService() {
		return service;
	}
	
	public RESTActorApplication(String name) {
		super();
		
		service = new ActorService(name);
		configure(service);
		service.setClientRunnable(new RESTActorClientRunnable(service.getServiceNodes(), service.getParallelismMin()*service.getParallelismFactor(), 10000));
		service.start();
		
		logger().info(String.format("%s - System started...", service.getName()));

		register(new AbstractBinder() {
			protected void configure() {
				bind(service).to(ActorService.class);
			}
		});

		register(new JacksonJsonProvider().configure(SerializationFeature.INDENT_OUTPUT, true));

		packages("actor4j.server.rest");
	}
	
	protected abstract void configure(ActorService service);
		
	@PreDestroy
	public void shutdown() {
		service.shutdownWithActors(true);
		logger().info(String.format("%s - System stopped...", service.getName()));
	}
}
