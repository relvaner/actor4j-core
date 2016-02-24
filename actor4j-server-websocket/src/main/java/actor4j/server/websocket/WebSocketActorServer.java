/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.server.websocket;

import static actor4j.core.utils.ActorLogger.logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.glassfish.tyrus.server.Server;

import actor4j.core.ActorService;
import actor4j.server.websocket.endpoints.ActorServerEndpoint;

public abstract class WebSocketActorServer {
	protected static ActorService service;
	
	public WebSocketActorServer(int port) {
		this(null, port);
	}
	
	public static ActorService getService() {
		return service;
	}
	
	public WebSocketActorServer(String name, int port) {
		super();
		
		service = new ActorService(name);
		configure(service);
		//service.setClientRunnable(new RESTActorClientRunnable(service.getServerURIs(), service.getParallelismMin()*service.getParallelismFactor(), 10000));
		service.start();
		logger().info(String.format("%s - System started...", service.getName()));
		
		Server server = new Server("localhost", port, "/websockets", null, ActorServerEndpoint.class);
	    try {
	        server.start();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	        logger().info(("Please press a key to stop the server."));
	        reader.readLine();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
			service.shutdownWithActors(true);
			logger().info(String.format("%s - System stopped...", service.getName()));

	        server.stop();
	    }
	}

	protected abstract void configure(ActorService service);
}
