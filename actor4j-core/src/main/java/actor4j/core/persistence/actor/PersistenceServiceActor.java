/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.persistence.actor;

import static actor4j.core.protocols.ActorProtocolTag.*;

import org.bson.Document;
import org.json.JSONArray;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class PersistenceServiceActor extends Actor {
	protected ActorSystem parent;
	
	protected String host;
	protected int port;
	protected String databaseName;
	
	protected MongoClient client;
	protected MongoDatabase database;
	protected MongoCollection<Document> events;
	protected MongoCollection<Document> states;
	
	public static final int EVENT    = 100;
	public static final int STATE    = 101;
	public static final int RECOVERY = INTERNAL_PERSISTENCE_RECOVERY;
	public static final int SUCCESS  = INTERNAL_PERSISTENCE_SUCCESS;
	public static final int FAILURE   = INTERNAL_PERSISTENCE_FAILURE;
	
	public PersistenceServiceActor(ActorSystem parent, String name, String host, int port, String databaseName) {
		super(name);
		this.parent = parent;
		this.host = host;
		this.port = port;
		this.databaseName = databaseName;
	}

	@Override
	public void preStart() {
		client = new MongoClient(host, port);
		
		database = client.getDatabase(databaseName);
		events = database.getCollection("persistence.events");
		states = database.getCollection("persistence.states");
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==EVENT) {
			try {
				JSONArray array = new JSONArray(message.valueAsString());
				System.out.println(array.getString(0));
				Document document = Document.parse(array.getString(0));
				events.insertOne(document);
				parent.send(new ActorMessage<Object>(null, SUCCESS, self(), message.source));
			}
			catch (Exception e) {
				e.printStackTrace();
				parent.send(new ActorMessage<Exception>(e, FAILURE, self(), message.source));
			}
		}
		else {
			try {
				Document document = Document.parse(message.valueAsString());
				states.insertOne(document);
				parent.send(new ActorMessage<Object>(null, SUCCESS, self(), message.source));
			}
			catch (Exception e) {
				e.printStackTrace();
				parent.send(new ActorMessage<Exception>(e, FAILURE, self(), message.source));
			}
		}
	}
	
	@Override
	public void postStop() {
		client.close();
	}
}
