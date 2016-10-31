/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.service.rest.client;

import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import actor4j.service.rest.client.utils.RESTClient;

public class RESTActorClient extends RESTClient {
	public static Response version(Client client, String uri) {
		return get(client, uri, "version", "application/json; charset=UTF-8", null);
	}
	
	public static Response hasActor(Client client, String uri, String param) {
		return get(client, uri, "hasactor/"+param, "application/json; charset=UTF-8", null);
	}
	
	public static Response getActor(Client client, String uri, String param) {
		return get(client, uri, "getactor/"+param, "application/json; charset=UTF-8", null);
	}
	
	public static Response sendMessage(Client client, String uri, String request) {
		return post(client, uri, "sendmessage", "application/json; charset=UTF-8", null, Entity.entity(request, MediaType.APPLICATION_JSON));
	}
	
	public static <I> Response sendMessage(Client client, String uri, I request) throws IOException, JsonProcessingException, JsonMappingException {
		return sendMessage(client, uri, new ObjectMapper().writeValueAsString(request));
	}
}
