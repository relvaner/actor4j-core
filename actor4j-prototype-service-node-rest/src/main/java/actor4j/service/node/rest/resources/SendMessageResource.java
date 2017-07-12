/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.service.node.rest.resources;

import java.io.IOException;
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
import actor4j.service.node.rest.databind.RESTActorResponse;
import actor4j.service.node.utils.TransferActorMessage;

@Path("/sendMessage")
public class SendMessageResource {
	@Context 
	ActorService service;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(String json) {
		TransferActorMessage message = null;
		String error = null;
		try {
			message = new ObjectMapper().readValue(json, TransferActorMessage.class);
		} catch (JsonParseException e) {
			error =  e.getMessage();
		} catch (JsonMappingException e) {
			error =  e.getMessage();
		} catch (IOException e) {
			error =  e.getMessage();
		}
		
		if (message!=null)
			service.sendAsServer(new RemoteActorMessage<Object>(message.value, message.tag, UUID.fromString(message.source),UUID.fromString(message.dest)));
		
		if (error==null)
			return Response.status(202).entity(
					new RESTActorResponse(
							RESTActorResponse.SUCCESS, 202, "", "The request was accepted and the message was send.")).build();
		else
			return Response.status(400).entity(
					new RESTActorResponse(
							RESTActorResponse.ERROR, 400, error, "The request was error prone.")).build();
	}
}
