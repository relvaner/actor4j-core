/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.service.rest.resources;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import actor4j.core.ActorSystem;

@Path("/version")
public class VersionResource {
	@Context 
	ActorSystem system;
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExample() {
		Map<String, Object> map = new HashMap<>();
		map.put("version", "1.0.0");
		
		return Response.ok().entity(map).build();
	}
}
