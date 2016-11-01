/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.service.websocket.endpoints;

import static actor4j.core.utils.ActorLogger.logger;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import actor4j.core.ActorService;
import actor4j.core.messages.RemoteActorMessage;
import actor4j.service.utils.TransferActorMessage;
import actor4j.service.websocket.WebSocketActorClientManager;

@ServerEndpoint(value = "/actor4j")
public abstract class ActorServerEndpoint {
	public static final char HAS_ACTOR    = '1';
	public static final char GET_ACTOR    = '2';
	public static final char SEND_MESSAGE = '3';
	public static final char CLIENT 	  = '4';
	
	protected ActorService service;
	
	protected abstract ActorService getService();
	
	@OnOpen
	public void onOpen(Session session) throws IOException {
		service = getService();
		
		logger().info(String.format("%s - Websocket-Session started...", service.getName()));
	}
	
    @OnMessage
    public String onMessage(String message, Session session) throws IOException {
    	logger().debug(message);
    	
    	String result = null;
    	
    	String data = message.substring(1);
    	switch (message.charAt(0)) {
    		case HAS_ACTOR    : {
    			result = (service.hasActor(data)) ? "1" : "0";
    			result = CLIENT + result;
    		}; break;
    		case GET_ACTOR    : {
    			UUID uuid = service.getActor(data);
    			result = (uuid!=null) ? uuid.toString() : "";
    			result = CLIENT + result;
    		}; break;
    		case SEND_MESSAGE : {
    			TransferActorMessage buf = null;
    			try {
    				buf = new ObjectMapper().readValue(data, TransferActorMessage.class);
    			} catch (Exception e) {
    				return CLIENT + "0";
    			}
    			
    			if (buf!=null) {
    				service.sendAsServer(new RemoteActorMessage<Object>(buf.value, buf.tag, UUID.fromString(buf.source), UUID.fromString(buf.dest)));
    			
    				result = CLIENT + "1";
    			}
    		}; break;
    		case CLIENT       : {
    			message = message.substring(1);
    			CompletableFuture<String> future = WebSocketActorClientManager.sessionMap.get(session);
    			future.complete(message);
    		};
    	}
    	
    	return result;
    }
    
    @OnClose
	public void onClose(Session session, CloseReason closeReason) {
    	logger().info(String.format("%s - Websocket-Session stopped...", service.getName()));
	}
    
    @OnError
    public void onError(Session session, Throwable t) {
        t.printStackTrace();
    }
}
