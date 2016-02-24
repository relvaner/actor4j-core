/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.server.websocket.example;

import actor4j.core.ActorService;
import actor4j.server.websocket.WebSocketActorServer;

public class ServerApplication extends WebSocketActorServer {
	public ServerApplication(String name, int port) {
		super(name, port);
	}
	
	@Override
	protected void configure(ActorService service) {
		service.setParallelismMin(1);
		service.setParallelismFactor(1);
		service.softMode();
	}
	
	public static void main(String[] args) {
		new ServerApplication(null, 8025);
	}
}
