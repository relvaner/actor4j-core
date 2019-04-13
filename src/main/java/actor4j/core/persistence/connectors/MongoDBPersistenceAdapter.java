/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actor4j.core.persistence.connectors;

import static actor4j.core.protocols.ActorProtocolTag.INTERNAL_PERSISTENCE_FAILURE;
import static actor4j.core.protocols.ActorProtocolTag.INTERNAL_PERSISTENCE_RECOVER;
import static actor4j.core.protocols.ActorProtocolTag.INTERNAL_PERSISTENCE_SUCCESS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;

import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import static actor4j.core.persistence.actor.PersistenceServiceActor.*;

public class MongoDBPersistenceAdapter extends PersistenceAdapter {
	protected MongoDatabase database;
	protected MongoCollection<Document> events;
	protected MongoCollection<Document> states;
	
	protected long lastTimeStamp;
	protected int indexIfEqualTimeStamp;
	
	public MongoDBPersistenceAdapter(ActorSystem parent, PersistenceConnector connector) {
		super(parent, connector);
	}

	@Override
	public void preStart(UUID id) {
		super.preStart(id);
		
		database = ((MongoDBPersistenceConnector)connector).client.getDatabase(connector.databaseName);
		events = database.getCollection("persistence.events");
		states = database.getCollection("persistence.states");
		
		lastTimeStamp = -1;
		indexIfEqualTimeStamp =  0;
	}
	
	public void checkTimeStamp(Document document) {
		long timestamp = (long)document.get("timeStamp");
		if (timestamp==lastTimeStamp)
			document.put("index", ++indexIfEqualTimeStamp);
		else {
			lastTimeStamp = timestamp;
			indexIfEqualTimeStamp = 0;
		}
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==PERSIST_EVENTS) {
			try {
				JSONArray array = new JSONArray(message.valueAsString());
				if (array.length()==1) {
					Document document = Document.parse(array.get(0).toString());
					checkTimeStamp(document);
					events.insertOne(document);
				}
				else {
					List<WriteModel<Document>> requests = new ArrayList<WriteModel<Document>>();
					for (Object obj : array) {
						Document document = Document.parse(obj.toString());
						checkTimeStamp(document);
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
				checkTimeStamp(document);
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
				
				FindIterable<Document> statesIterable = states.find(new Document("persistenceId", message.valueAsString())).sort(new Document("timeStamp", -1).append("index", -1)).limit(1);
				document = statesIterable.first();
				if (document!=null) {
					JSONObject stateValue = new JSONObject(document.toJson());
					stateValue.remove("_id");
					long stateTimeStamp = stateValue.getJSONObject("timeStamp").getLong("$numberLong");
					int stateIndex = stateValue.getInt("index");
					stateValue.put("timeStamp", stateTimeStamp);
					obj.put("state", stateValue);
					
					FindIterable<Document> eventsIterable = events
							.find(new Document("persistenceId", message.valueAsString()).append("timeStamp", new Document("$gte", stateTimeStamp)))
							.sort(new Document("timeStamp", 1).append("index", 1));
					JSONArray array = new JSONArray();
					MongoCursor<Document> cursor = eventsIterable.iterator();
					while (cursor.hasNext()) {
						document = cursor.next();
						JSONObject eventValue = new JSONObject(document.toJson());
						eventValue.remove("_id");
						long timeStamp = eventValue.getJSONObject("timeStamp").getLong("$numberLong");
						int index = eventValue.getInt("index");
						if (timeStamp==stateTimeStamp) {
							if (index>stateIndex) {
								eventValue.put("timeStamp", timeStamp);
								array.put(eventValue);
							}
						}
						else {
							eventValue.put("timeStamp", timeStamp);
							array.put(eventValue);
						}
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
}
