/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.data;

import java.io.IOException;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import actor4j.core.actors.ResourceActor;
import actor4j.core.messages.ActorMessage;

import static actor4j.core.actors.ActorWithCache.*;

public class DataAccessActor<K, V> extends ResourceActor {
	protected MongoClient client;
	protected String databaseName;
	protected Class<V> valueType;
	
	protected static final int HAS_ONE     = 304;
	protected static final int INSERT_ONE  = 305;
	protected static final int REPLACE_ONE = 306;
	protected static final int UPDATE_ONE  = 307;
	protected static final int FIND_ONE    = 308;
	
	public DataAccessActor(String name, MongoClient client, String databaseName, Class<V> valueType) {
		super(name);
		this.client = client;
		this.databaseName = databaseName;
		this.valueType = valueType;
	}
	
	public DataAccessActor(MongoClient client, String databaseName, Class<V> valueType) {
		this(null, client, databaseName, valueType);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null && message.value instanceof DataAccessObject) {
			@SuppressWarnings("unchecked")
			DataAccessObject<K,V> obj = (DataAccessObject<K,V>)message.value;
			if (message.tag==FIND_ONE || message.tag==GET) {
				obj.value = findOne(obj.filter, obj.collectionName, valueType);
				tell(obj, FIND_ONE, message.source);
			}
			else if (message.tag==SET) {
				if (!hasOne(obj.filter, obj.collectionName))
					insertOne(obj.value, obj.collectionName);
				else
					replaceOne(obj.filter, obj.value, obj.collectionName);
			}
			else if (message.tag==UPDATE_ONE || message.tag==UPDATE)
				updateOne(obj.filter, obj.update, obj.collectionName);
			else if (message.tag==INSERT_ONE)
				insertOne(obj.value, obj.collectionName);
			else if (message.tag==HAS_ONE) {
				obj.reserved = hasOne(obj.filter, obj.collectionName);
				tell(obj, FIND_ONE, message.source);
			}
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
	
	public boolean hasOne(Document filter, String collectionName) {
		boolean result = false;
		
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		Document document = collection.find(filter).first();
		
		if (document!=null)
			result = true;
		
		return result;
	}
	
	public void insertOne(V value, String collectionName) {
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		try {
			Document document = Document.parse(new ObjectMapper().writeValueAsString(value));
			collection.insertOne(document);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public void replaceOne(Document filter, V value, String collectionName) {
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		try {
			Document document = Document.parse(new ObjectMapper().writeValueAsString(value));
			collection.replaceOne(filter, document);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public void updateOne(Document filter, Document update, String collectionName) {
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		collection.updateOne(filter, update);
	}
	
	public V findOne(Document filter, String collectionName, Class<V> valueType) {
		V result = null;
		
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		Document document = collection.find(filter).first();
		
		if (document!=null)
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				
				result = objectMapper.readValue(document.toJson(), valueType);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return result;
	}
}
