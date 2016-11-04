/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.service.node.rest;

import static actor4j.core.utils.ActorLogger.*;

import javax.annotation.PreDestroy;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import actor4j.core.ActorService;

public abstract class RESTActorService extends ResourceConfig {
	protected final ActorService service;

	public abstract ActorService getService();
	
	public RESTActorService() {
		super();
		
		service = getService();
		
		logger().info(String.format("%s - REST-Service started...", service.getName()));
		
		packages("actor4j.service.node.rest");

		register(new AbstractBinder() {
			protected void configure() {
				bind(service).to(ActorService.class);
			}
		});

		register(new JacksonJsonProvider().configure(SerializationFeature.INDENT_OUTPUT, true));
	}
	
	@PreDestroy
	public void shutdown() {
		logger().info(String.format("%s - REST-Service stopped...", service.getName()));
	}
}
