/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.service.node.template.websocket;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class DefaultWebsocketActorService extends Configurator {
	protected static DefaultActorServerEndpoint serverEndpoint  = new DefaultActorServerEndpoint();
	
    @SuppressWarnings("unchecked")
	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)serverEndpoint;
    }
}
