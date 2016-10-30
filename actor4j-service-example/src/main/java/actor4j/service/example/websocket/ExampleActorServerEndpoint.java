package actor4j.service.example.websocket;

import javax.websocket.server.ServerEndpoint;

import actor4j.core.ActorService;
import actor4j.service.example.startup.ExampleActorService;
import actor4j.service.websocket.endpoints.ActorServerEndpoint;

@ServerEndpoint(value = "/actor4j", configurator=ExampleWebsocketActorService.class)
public class ExampleActorServerEndpoint extends ActorServerEndpoint {
	@Override
	protected ActorService getService() {
		return ExampleActorService.getService();
	}
}
