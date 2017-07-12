/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
	public static Response _version(Client client, String uri) {
		return get(client, uri, "version", "application/json; charset=UTF-8", null);
	}
	
	public static Response _hasActor(Client client, String uri, String uuid) {
		return get(client, uri, "hasActor/"+uuid, "application/json; charset=UTF-8", null);
	}
	
	public static Response _getActorFromAlias(Client client, String uri, String alias) {
		return get(client, uri, "getActorFromAlias/"+alias, "application/json; charset=UTF-8", null);
	}
	
	public static Response _getActorFromPath(Client client, String uri, String path) {
		return get(client, uri, "getActorFromPath/"+path, "application/json; charset=UTF-8", null);
	}
	
	public static Response _sendMessage(Client client, String uri, String request) {
		return post(client, uri, "sendMessage", "application/json; charset=UTF-8", null, Entity.entity(request, MediaType.APPLICATION_JSON));
	}
	
	public static <I> Response _sendMessage(Client client, String uri, I request) throws IOException, JsonProcessingException, JsonMappingException {
		return _sendMessage(client, uri, new ObjectMapper().writeValueAsString(request));
	}
	
	public static RESTActorResponse version(Client client, String uri) throws JsonParseException, JsonMappingException, IOException {
		Response response = _version(client, uri);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static RESTActorResponse hasActor(Client client, String uri, String uuid) throws JsonParseException, JsonMappingException, IOException {
		Response response = _hasActor(client, uri, uuid);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static RESTActorResponse getActorFromAlias(Client client, String uri, String alias) throws JsonParseException, JsonMappingException, IOException {
		Response response = _getActorFromAlias(client, uri, alias);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static RESTActorResponse getActorFromPath(Client client, String uri, String path) throws JsonParseException, JsonMappingException, IOException {
		Response response = _getActorFromPath(client, uri, path);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static RESTActorResponse sendMessage(Client client, String uri, String request) throws JsonParseException, JsonMappingException, IOException {
		Response response = _sendMessage(client, uri, request);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
	
	public static <I> RESTActorResponse sendMessage(Client client, String uri, I request) throws IOException, JsonProcessingException, JsonMappingException {
		Response response = _sendMessage(client, uri, request);
		return new ObjectMapper().readValue(response.readEntity(String.class), RESTActorResponse.class);
	}
}
