/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.server.websocket.example;

import static actor4j.core.utils.ActorLogger.logger;

import java.net.URI;

import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import actor4j.server.websocket.WebSocketActorClientManager;
import actor4j.server.websocket.endpoints.ActorClientEndpoint;

public class ClientApplication {
	public static void main(String[] args) {
        ClientManager client = ClientManager.createClient();
        try {
            Session session = client.connectToServer(ActorClientEndpoint.class, new URI("ws://localhost:8025/websockets/actor4j"));
            logger().debug(WebSocketActorClientManager.sendText(session, WebSocketActorClientManager.GET_ACTOR));
            logger().debug(WebSocketActorClientManager.sendText(session, WebSocketActorClientManager.HAS_ACTOR));
            session.close();
 
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
}
