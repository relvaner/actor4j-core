/*
 * Copyright (c) 2015-2017, David A. Bauer
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

@Path("/getActorFromPath/{path}")
public class GetActorFromPathResource {
	@Context
	ActorService service;

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getActorFromPath(@PathParam("path") String path) {
		UUID uuid = service.getActorFromPath(path);
		if (uuid != null)
			return Response.ok().entity(
					new RESTActorResponse(
							RESTActorResponse.SUCCESS, 200, uuid.toString(), "")).build();
		else
			return Response.status(404).entity(
					new RESTActorResponse(
							RESTActorResponse.FAIL, 404, "", "The actor for a given path was not found.")).build();
	}
}
