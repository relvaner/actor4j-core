/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.service.node.template.startup;

import static actor4j.core.utils.ActorLogger.logger;

import org.apache.log4j.Level;

import actor4j.core.ActorService;
import actor4j.service.node.websocket.WebsocketActorClientRunnable;

public class DefaultActorService {
	protected static ActorService service;
	
	private DefaultActorService() {
		super();
	}
	
	public static void start() {
		service = new ActorService();
		
		config(service);
		service.setClientRunnable(new WebsocketActorClientRunnable(service.getServiceNodes(), service.getParallelismMin()*service.getParallelismFactor(), 10000));
		
		logger().setLevel(Level.DEBUG);
		logger().info(String.format("%s - Service started...", service.getName()));
		service.start();
	}
	
	protected static void config(ActorService service) {
		/* Insert your code here! */ 
	}
	
	public static void stop() {
		service.shutdownWithActors(true);
		((WebsocketActorClientRunnable)service.getClientRunnable()).closeAll();
		logger().info(String.format("%s - Service stopped...", service.getName()));
	}
	
	public static ActorService getService() {
		return service;
	}
}
