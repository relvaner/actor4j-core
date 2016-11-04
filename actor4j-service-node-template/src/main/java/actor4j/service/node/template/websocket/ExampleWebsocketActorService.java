package actor4j.service.node.template.websocket;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class ExampleWebsocketActorService extends Configurator {
	protected static ExampleActorServerEndpoint serverEndpoint  = new ExampleActorServerEndpoint();
	
    @SuppressWarnings("unchecked")
	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)serverEndpoint;
    }
}
