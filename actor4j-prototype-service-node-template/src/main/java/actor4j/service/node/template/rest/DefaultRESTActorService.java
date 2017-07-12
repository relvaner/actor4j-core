/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.service.node.template.rest;

import javax.ws.rs.ApplicationPath;

import actor4j.core.ActorService;
import actor4j.service.node.rest.RESTActorService;
import actor4j.service.node.template.startup.DefaultActorService;


@ApplicationPath("api")
public class DefaultRESTActorService extends RESTActorService {
	@Override
	public ActorService getService() {
		return DefaultActorService.getService();
	}
}
