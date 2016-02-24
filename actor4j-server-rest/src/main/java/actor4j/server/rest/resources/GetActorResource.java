/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.rest.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import actor4j.core.ActorService;

@Path("/getactor/{alias}")
public class GetActorResource {
	@Context 
	ActorService service;
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActor(@PathParam("alias") String alias) {
		Map<String, String> map = new HashMap<>();
		UUID uuid = service.getActor(alias);
		if (uuid!=null)
			map.put("result", uuid.toString());
		else
			map.put("result", "");
			
		return Response.ok().entity(map).build();
	}
}
