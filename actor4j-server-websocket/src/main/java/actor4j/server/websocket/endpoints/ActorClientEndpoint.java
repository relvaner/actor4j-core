/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.server.websocket.endpoints;

import static actor4j.core.utils.ActorLogger.logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import actor4j.server.websocket.WebSocketActorClientManager;

@ClientEndpoint
public class ActorClientEndpoint {
	@OnOpen
	public void onOpen(Session session) throws IOException {
	}
	 
	@OnMessage
	public String onMessage(String message, Session session) {
		logger().debug(message);
		message = message.substring(1);
		CompletableFuture<String> future = WebSocketActorClientManager.sessionMap.get(session);
		future.complete(message);
		return null;
	}
	
	@OnClose
	public void onClose(Session session, CloseReason closeReason) throws InterruptedException {
	}
	
	@OnError
    public void onError(Session session, Throwable t) {
        t.printStackTrace();
    }
}
