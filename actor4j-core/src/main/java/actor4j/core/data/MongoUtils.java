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

public final class MongoUtils {
	public static boolean hasOne(Document filter, MongoClient client, String databaseName, String collectionName) {
		boolean result = false;
		
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		Document document = collection.find(filter).first();
		
		if (document!=null)
			result = true;
		
		return result;
	}
	
	public static  <V> void insertOne(V value, MongoClient client, String databaseName, String collectionName) {
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		try {
			Document document = Document.parse(new ObjectMapper().writeValueAsString(value));
			collection.insertOne(document);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public static <V> void replaceOne(Document filter, V value, MongoClient client, String databaseName, String collectionName) {
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		try {
			Document document = Document.parse(new ObjectMapper().writeValueAsString(value));
			collection.replaceOne(filter, document);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public static <V> void updateOne(Document filter, Document update, MongoClient client, String databaseName, String collectionName) {
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		collection.updateOne(filter, update);
	}
	
	public static <V> V findOne(Document filter, MongoClient client, String databaseName, String collectionName, Class<V> valueType) {
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
	
	public static <V> Document convertToDocument(V value) {
		Document result = null;
		try {
			result = Document.parse(new ObjectMapper().writeValueAsString(value));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
