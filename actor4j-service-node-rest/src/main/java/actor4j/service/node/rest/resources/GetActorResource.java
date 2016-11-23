/*
 * Copyright (c) 2015-2016, David A. Bauer
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

@Path("/getactor/{alias}")
public class GetActorResource {
	@Context
	ActorService service;

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getActor(@PathParam("alias") String alias) {
		UUID uuid = service.getActor(alias);
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
