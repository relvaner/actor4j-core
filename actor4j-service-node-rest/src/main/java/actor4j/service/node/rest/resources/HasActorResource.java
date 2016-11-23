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

@Path("/hasactor/{uuid}")
public class HasActorResource {
	@Context 
	ActorService service;
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hasActor(@PathParam("uuid") String uuid) {
		try {
			UUID.fromString(uuid);
		}
		catch (IllegalArgumentException e) {
			return Response.serverError().entity(
					new RESTActorResponse(
							RESTActorResponse.ERROR, 500, e.getMessage(), "The request was error prone.")).build();
		}
		
		if (service.hasActor(uuid))
			return Response.ok().entity(
					new RESTActorResponse(
							RESTActorResponse.SUCCESS, 200, "true", "The actor was found.")).build();
		else
			return Response.status(404).entity(
					new RESTActorResponse(
							RESTActorResponse.FAIL, 404, "false", "The actor was not found.")).build();
	}
}
