/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.resources;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import actor4j.core.ActorService;

@Path("/hasactor/{uuid}")
public class HasActorResource {
	@Context 
	ActorService service;
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hasActor(@PathParam("uuid") String uuid) {
		Map<String, Boolean> map = new HashMap<>();
		if (service.hasActor(uuid))
			map.put("result", true);
		else
			map.put("result", false);
		
		return Response.ok().entity(map).build();
	}
}
