/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.persistence.actor;

import static actor4j.core.protocols.ActorProtocolTag.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;

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
	
	public static final int PERSIST_EVENTS = 100;
	public static final int PERSIST_STATE  = 101;
	public static final int RECOVER  	   = 102;
	
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
		if (message.tag==PERSIST_EVENTS) {
			try {
				JSONArray array = new JSONArray(message.valueAsString());
				if (array.length()==1) {
					Document document = Document.parse(array.get(0).toString());
					events.insertOne(document);
				}
				else {
					List<WriteModel<Document>> requests = new ArrayList<WriteModel<Document>>();
					for (Object obj : array) {
						Document document = Document.parse(obj.toString());
						requests.add(new InsertOneModel<Document>(document));
					}
					events.bulkWrite(requests);
				}
				parent.send(new ActorMessage<Object>(null, INTERNAL_PERSISTENCE_SUCCESS, self(), message.source));
			}
			catch (Exception e) {
				e.printStackTrace();
				parent.send(new ActorMessage<Exception>(e, INTERNAL_PERSISTENCE_FAILURE, self(), message.source));
			}
		}
		else if (message.tag==PERSIST_STATE){
			try {
				Document document = Document.parse(message.valueAsString());
				states.insertOne(document);
				parent.send(new ActorMessage<Object>(null, INTERNAL_PERSISTENCE_SUCCESS, self(), message.source));
			}
			catch (Exception e) {
				e.printStackTrace();
				parent.send(new ActorMessage<Exception>(e, INTERNAL_PERSISTENCE_FAILURE, self(), message.source));
			}
		}
		else if (message.tag==RECOVER) {
			try {
				JSONObject obj = new JSONObject();
				Document document = null;
				
				FindIterable<Document> statesIterable = states.find(new Document("persistenceId", message.valueAsString())).sort(new Document("timeStamp", -1)).limit(1);
				document = statesIterable.first();
				if (document!=null) {
					JSONObject stateValue = new JSONObject(document.toJson());
					stateValue.remove("_id");
					long timeStamp = stateValue.getJSONObject("timeStamp").getLong("$numberLong");
					stateValue.put("timeStamp", timeStamp);
					obj.put("state", stateValue);
					
					FindIterable<Document> eventsIterable = events.find(new Document("persistenceId", message.valueAsString()).append("timeStamp", new Document("$gte", timeStamp))).sort(new Document("timeStamp", -1));
					JSONArray array = new JSONArray();
					MongoCursor<Document> cursor = eventsIterable.iterator();
					while (cursor.hasNext()) {
						document = cursor.next();
						JSONObject eventValue = new JSONObject(document.toJson());
						eventValue.remove("_id");
						timeStamp = eventValue.getJSONObject("timeStamp").getLong("$numberLong");
						eventValue.put("timeStamp", timeStamp);
						array.put(eventValue);
					}
					cursor.close();
					obj.put("events", array);
				}
				else
					obj.put("state", new JSONObject());
				
				parent.send(new ActorMessage<String>(obj.toString(), INTERNAL_PERSISTENCE_RECOVER, self(), message.source));
			}
			catch (Exception e) {
				e.printStackTrace();
				JSONObject obj = new JSONObject();
				obj.put("error", e.getMessage());
				parent.send(new ActorMessage<String>(obj.toString(), INTERNAL_PERSISTENCE_RECOVER, self(), message.source));
			}
		}
	}
	
	@Override
	public void postStop() {
		client.close();
	}
}
