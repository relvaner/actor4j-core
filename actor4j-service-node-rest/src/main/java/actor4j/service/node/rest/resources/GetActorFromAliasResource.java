/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.service.node.rest.resources;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import actor4j.core.ActorService;
import actor4j.service.node.rest.databind.RESTActorResponse;

@Path("/getActorFromAlias/{alias}")
public class GetActorFromAliasResource {
	@Context
	ActorService service;

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getActorFromAlias(@PathParam("alias") String alias) {
		UUID uuid = service.getActorFromAlias(alias);
		if (uuid != null)
			return Response.ok().entity(
					new RESTActorResponse(
							RESTActorResponse.SUCCESS, 200, uuid.toString(), "")).build();
		else
			return Response.status(404).entity(
					new RESTActorResponse(
							RESTActorResponse.FAIL, 404, "", "The actor for a given alias was not found.")).build();
	}
}
