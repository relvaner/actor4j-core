/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.service.example.startup;

import static actor4j.core.utils.ActorLogger.logger;

import org.apache.log4j.Level;

import actor4j.core.ActorService;

public class ExampleActorService {
	protected static ActorService service;
	
	private ExampleActorService() {
		super();
	}
	
	public static void start() {
		service = new ActorService();
		
		config(service);
		
		logger().setLevel(Level.DEBUG);
		logger().info(String.format("%s - Service started...", service.getName()));
		service.start();
	}
	
	protected static void config(ActorService service) {
		/* Insert your code here! */ 
	}
	
	public static void stop() {
		service.shutdownWithActors(true);
		logger().info(String.format("%s - Service stopped...", service.getName()));
	}
	
	public static ActorService getService() {
		return service;
	}
}
