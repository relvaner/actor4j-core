/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.data.access;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOneModel;

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
		insertOne(value, client, databaseName, collectionName, null);
	}
	
	public static <V> void replaceOne(Document filter, V value, MongoClient client, String databaseName, String collectionName) {
		replaceOne(filter, value, client, databaseName, collectionName, null);
	}
	
	public static <V> void updateOne(Document filter, Document update, MongoClient client, String databaseName, String collectionName) {
		updateOne(filter, update, client, databaseName, collectionName, null);
	}
	
	public static <V> void deleteOne(Document filter, MongoClient client, String databaseName, String collectionName) {
		deleteOne(filter, client, databaseName, collectionName, null);
	}
	
	public static  <V> void insertOne(V value, MongoClient client, String databaseName, String collectionName, MongoBufferedBulkWriter bulkWriter) {
		try {
			Document document = Document.parse(new ObjectMapper().writeValueAsString(value));
			if (bulkWriter!=null)
				bulkWriter.write(new InsertOneModel<>(document));
			else {
				MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
				collection.insertOne(document);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public static <V> void replaceOne(Document filter, V value, MongoClient client, String databaseName, String collectionName, MongoBufferedBulkWriter bulkWriter) {
		try {
			Document document = Document.parse(new ObjectMapper().writeValueAsString(value));
			if (bulkWriter!=null)
				bulkWriter.write(new ReplaceOneModel<>(filter, document));
			else {
				MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
				collection.replaceOne(filter, document);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public static <V> void updateOne(Document filter, Document update, MongoClient client, String databaseName, String collectionName, MongoBufferedBulkWriter bulkWriter) {
		if (bulkWriter!=null)
			bulkWriter.write(new UpdateOneModel<>(filter, update));
		else {
			MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
			collection.updateOne(filter, update);
		}
	}
	
	public static <V> void deleteOne(Document filter, MongoClient client, String databaseName, String collectionName, MongoBufferedBulkWriter bulkWriter) {
		if (bulkWriter!=null)
			bulkWriter.write(new DeleteOneModel<>(filter));
		else {
			MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
			collection.deleteOne(filter);
		}
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
	
	public static <V> List<V> find(Document filter, Document sort, Document projection, int skip, int limit, MongoClient client, String databaseName, String collectionName, Class<V> valueType) {
		List<V> result = new LinkedList<>();
		
		List<Document> documents = new LinkedList<>();
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collectionName);
		
		FindIterable<Document> iterable = null;
		if (filter!=null)
			iterable = collection.find(filter);
		else
			iterable = collection.find();
		
		if (sort!=null)
			iterable = iterable.sort(sort);
		if (projection!=null)
			iterable = iterable.projection(projection);
		if (skip>0)
			iterable = iterable.skip(skip);
		if (limit>0)
			iterable = iterable.limit(limit);
		
		iterable.forEach((Block<Document>) document -> {documents.add(document);});
		
		for (Document document : documents)
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				
				result.add(objectMapper.readValue(document.toJson(), valueType));
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return result;
	}
	
	public static <V> List<V> findAll(Document filter, Document sort, Document projection, MongoClient client, String databaseName, String collectionName, Class<V> valueType) {
		return find(filter, sort, projection, 0, 0, client, databaseName, collectionName, valueType);
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
