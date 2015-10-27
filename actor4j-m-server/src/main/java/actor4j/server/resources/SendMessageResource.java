/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import actor4j.core.ActorService;
import actor4j.core.messages.RemoteActorMessage;
import actor4j.server.RESTActorMessage;

@Path("/sendmessage")
public class SendMessageResource {
	@Context 
	ActorService service;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(String json) {
		RESTActorMessage message = null;
		try {
			message = new ObjectMapper().readValue(json, RESTActorMessage.class);
		} catch (JsonParseException e) {
			HashMap<String, String> map = new HashMap<>();
			map.put("error", e.getMessage());
			return Response.serverError().entity(map).build();
		} catch (JsonMappingException e) {
			HashMap<String, String> map = new HashMap<>();
			map.put("error", e.getMessage());
			return Response.serverError().entity(map).build();
		} catch (IOException e) {
			HashMap<String, String> map = new HashMap<>();
			map.put("error", e.getMessage());
			return Response.serverError().entity(map).build();
		}
		
		if (message!=null)
			service.sendAsServer(new RemoteActorMessage<Object>(message.value, message.tag, UUID.fromString(message.source),UUID.fromString(message.dest)));
		
		HashMap<String, String> map = new HashMap<>();
		map.put("result", "accepted");
		return Response.accepted().entity(map).build();
	}
}
