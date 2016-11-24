/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.service.node.rest.client;

import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import actor4j.service.node.rest.client.utils.RESTClient;
import actor4j.service.node.rest.databind.RESTActorResponse;

public class RESTActorClient extends RESTClient {
	public static Response version(Client client, String uri) {
		return get(client, uri, "version", "application/json; charset=UTF-8", null);
	}
	
	public static Response hasActor(Client client, String uri, String uuid) {
		return get(client, uri, "hasactor/"+uuid, "application/json; charset=UTF-8", null);
	}
	
	public static Response getActor(Client client, String uri, String alias) {
		return get(client, uri, "getactor/"+alias, "application/json; charset=UTF-8", null);
	}
	
	public static Response sendMessage(Client client, String uri, String request) {
		return post(client, uri, "sendmessage", "application/json; charset=UTF-8", null, Entity.entity(request, MediaType.APPLICATION_JSON));
	}
	
	public static <I> Response sendMessage(Client client, String uri, I request) throws IOException, JsonProcessingException, JsonMappingException {
		return sendMessage(client, uri, new ObjectMapper().writeValueAsString(request));
	}
	
	public static RESTActorResponse om_version(Client client, String uri) throws JsonParseException, JsonMappingException, IOException {
		Response response = version(client, uri);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static RESTActorResponse om_hasActor(Client client, String uri, String uuid) throws JsonParseException, JsonMappingException, IOException {
		Response response = hasActor(client, uri, uuid);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static RESTActorResponse om_getActor(Client client, String uri, String alias) throws JsonParseException, JsonMappingException, IOException {
		Response response = getActor(client, uri, alias);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static RESTActorResponse om_sendMessage(Client client, String uri, String request) throws JsonParseException, JsonMappingException, IOException {
		Response response = sendMessage(client, uri, request);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static <I> RESTActorResponse om_sendMessage(Client client, String uri, I request) throws IOException, JsonProcessingException, JsonMappingException {
		Response response = sendMessage(client, uri, request);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
}
