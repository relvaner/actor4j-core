/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
