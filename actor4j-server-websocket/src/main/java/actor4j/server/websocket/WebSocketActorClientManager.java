/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.server.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.websocket.Session;

import actor4j.server.websocket.endpoints.ActorServerEndpoint;

public class WebSocketActorClientManager {
	public static Map<Session, CompletableFuture<String>> sessionMap;
	
	public static final String HAS_ACTOR    = String.valueOf(ActorServerEndpoint.HAS_ACTOR);
	public static final String GET_ACTOR    = String.valueOf(ActorServerEndpoint.GET_ACTOR);
	public static final String SEND_MESSAGE = String.valueOf(ActorServerEndpoint.SEND_MESSAGE);
	
	static {
		sessionMap = new ConcurrentHashMap<>();
	}
	
	private WebSocketActorClientManager() {
	}
	
	public static CompletableFuture<String> sendText(Session session, String message) throws IOException, InterruptedException, ExecutionException {
		CompletableFuture<String> result = new CompletableFuture<>();
		sessionMap.put(session, result);
        session.getBasicRemote().sendText(message);
        return result;
	}
	
	public static CompletableFuture<String> sendText(Session session, String tag, String message) throws IOException, InterruptedException, ExecutionException {
		return sendText(session, tag+message);
	}
}
