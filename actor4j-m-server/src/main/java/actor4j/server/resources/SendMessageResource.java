package actor4j.server.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

import actor4j.core.ActorSystem;
import actor4j.core.RemoteActorMessage;
import actor4j.server.RESTActorMessage;

@Path("/sendmessage")
public class SendMessageResource {
	@Context 
	ActorSystem system;
	
	@SuppressWarnings("unchecked")
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
			system.sendAsServer(new RemoteActorMessage((LinkedHashMap<String, Object>)message.value, message.tag, UUID.fromString(message.source),UUID.fromString( message.dest)));
		
		HashMap<String, String> map = new HashMap<>();
		map.put("result", "accepted");
		return Response.accepted().entity(map).build();
	}
}
