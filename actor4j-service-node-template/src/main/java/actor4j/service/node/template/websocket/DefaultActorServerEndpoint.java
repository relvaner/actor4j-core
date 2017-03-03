/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.service.node.template.websocket;

import javax.websocket.server.ServerEndpoint;

import actor4j.core.ActorService;
import actor4j.service.node.template.startup.DefaultActorService;
import actor4j.service.node.websocket.endpoints.ActorServerEndpoint;

@ServerEndpoint(value = "/actor4j", configurator=DefaultWebsocketActorService.class)
public class DefaultActorServerEndpoint extends ActorServerEndpoint {
	@Override
	protected ActorService getService() {
		return DefaultActorService.getService();
	}
}
