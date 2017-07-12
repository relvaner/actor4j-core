/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.service.node.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import actor4j.service.node.rest.databind.RESTActorResponse;

@Path("/ping")
public class PingResource {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExample() {
		return Response.ok().entity(new RESTActorResponse(RESTActorResponse.SUCCESS, 200, "1", "pong")).build();
	}
}
