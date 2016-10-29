/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import actor4j.core.ActorClientRunnable;
import actor4j.core.ActorServiceNode;
import actor4j.core.messages.ActorMessage;
import actor4j.server.core.TransferActorMessage;

public class RESTActorClientRunnable implements ActorClientRunnable {
	protected List<ActorServiceNode> serviceNodes;
	protected LoadingCache<UUID, Integer> cache;
	protected LoadingCache<String, UUID> cacheAlias;
	
	public RESTActorClientRunnable(final List<ActorServiceNode> serviceNodes, int concurrencyLevel, int cachesize) {
		this.serviceNodes = serviceNodes;
		
		cache =  CacheBuilder.newBuilder()
				.maximumSize(cachesize)
				.concurrencyLevel(concurrencyLevel)
				.build(new CacheLoader<UUID, Integer>() {
			@Override
			public Integer load(UUID dest) throws Exception {
				String uuid = dest.toString();
				
				Client client = RESTActorClient.createClient();
				int found = -1;
				int i = 0;
				for (ActorServiceNode serviceNode : serviceNodes) {
					Response response = RESTActorClient.hasActor(client, serviceNode.getUri(), uuid);
					if (response.getStatus()==Response.Status.OK.getStatusCode()) {
						@SuppressWarnings("unchecked")
						HashMap<String, Boolean> map = (HashMap<String, Boolean>)(new ObjectMapper().readValue(response.readEntity(String.class), HashMap.class));
						if (map.get("result")) {
							found = i;
							break;
						}	
					}
					i++;
				}
				client.close();
				
				return found;
			}
		});
		
		cacheAlias =  CacheBuilder.newBuilder()
				.maximumSize(cachesize)
				.concurrencyLevel(concurrencyLevel)
				.build(new CacheLoader<String, UUID>() {
			@Override
			public UUID load(String alias) throws Exception {
				UUID result = null;
				Client client = RESTActorClient.createClient();
				int i = 0;
				for (ActorServiceNode serviceNode : serviceNodes) {
					Response response = RESTActorClient.getActor(client, serviceNode.getUri(), alias);
					if (response.getStatus()==Response.Status.OK.getStatusCode()) {
						@SuppressWarnings("unchecked")
						HashMap<String, String> map = (HashMap<String, String>)(new ObjectMapper().readValue(response.readEntity(String.class), HashMap.class));
						String uuid = map.get("result");
						if (!uuid.equals("")) {
							result = UUID.fromString(uuid);
							final int found = i;
							cache.get(result, new Callable<Integer>() {
								@Override
								public Integer call() throws Exception {
									return found;
								}
							});
							break;
						}	
					}
					i++;
				}
				client.close();
				
				return result;
			}
		});
	}
	
	@Override
	public void run(ActorMessage<?> message, String alias) {
		if (alias!=null) {
			UUID uuid = null;
			if ((uuid=cacheAlias.getUnchecked(alias))!=null)
				message.dest = uuid;
			else
				return;	
		}
		
		int index;
		if ((index=cache.getUnchecked(message.dest))!=-1) {
			try {
				Client client = RESTActorClient.createClient();
				RESTActorClient.sendMessage(client, serviceNodes.get(index).getUri(), new TransferActorMessage(message.value, message.tag, message.source.toString(), message.dest.toString()));
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
