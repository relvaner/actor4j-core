/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.data.access;

import com.mongodb.MongoClient;

import actor4j.core.actors.ResourceActor;
import actor4j.core.messages.ActorMessage;

import static actor4j.core.actors.ActorWithCache.*;
import static actor4j.core.data.access.MongoUtils.*;

import java.util.HashMap;
import java.util.Map;

public class DataAccessActor<K, V> extends ResourceActor {
	protected MongoClient client;
	protected String databaseName;
	protected boolean bulkWrite;
	protected boolean bulkOrdered;
	protected int bulkSize;
	protected Class<V> valueType;
	protected Map<String, MongoBufferedBulkWriter> bulkWriters;
	
	public static final int HAS_ONE     = 304;
	public static final int INSERT_ONE  = 305;
	public static final int REPLACE_ONE = 306;
	public static final int UPDATE_ONE  = 307;
	public static final int FIND_ONE    = 308;
	public static final int FLUSH       = 309;
	
	public DataAccessActor(String name, MongoClient client, String databaseName, boolean bulkWrite, boolean bulkOrdered, int bulkSize, Class<V> valueType) {
		super(name);
		this.client = client;
		this.databaseName = databaseName;
		this.bulkWrite = bulkWrite;
		this.bulkOrdered = bulkOrdered;
		this.bulkSize = bulkSize;
		this.valueType = valueType;
		
		bulkWriters = new HashMap<>();
	}
	
	public DataAccessActor(MongoClient client, String databaseName, boolean bulkWrite, boolean bulkOrdered, int bulkSize, Class<V> valueType) {
		this(null, client, databaseName, bulkWrite, bulkOrdered, bulkSize, valueType);
	}
	
	public DataAccessActor(String name, MongoClient client, String databaseName, Class<V> valueType) {
		this(name, client, databaseName, false, true, 0, valueType);
	}
	
	public DataAccessActor(MongoClient client, String databaseName, Class<V> valueType) {
		this(null, client, databaseName, valueType);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null && message.value instanceof DataAccessObject) {
			@SuppressWarnings("unchecked")
			DataAccessObject<K,V> obj = (DataAccessObject<K,V>)message.value;
			
			MongoBufferedBulkWriter bulkWriter = null;
			if (bulkWrite) {
				bulkWriter = bulkWriters.get(obj.collectionName);
				if (bulkWriter==null) {
					bulkWriter = new MongoBufferedBulkWriter(client.getDatabase(databaseName).getCollection(obj.collectionName), bulkOrdered, bulkSize);
					bulkWriters.put(obj.collectionName, bulkWriter);
				}
			}
			
			if (message.tag==FIND_ONE || message.tag==GET) {
				obj.value = findOne(obj.filter, client, databaseName, obj.collectionName, valueType);
				tell(obj, FIND_ONE, message.source);
			}
			else if (message.tag==SET) {
				if (!((boolean)obj.reserved) && !hasOne(obj.filter, client, databaseName, obj.collectionName))
					insertOne(obj.value, client, databaseName, obj.collectionName, bulkWriter);
				else
					replaceOne(obj.filter, obj.value, client, databaseName, obj.collectionName, bulkWriter);
			}
			else if (message.tag==UPDATE_ONE || message.tag==UPDATE)
				updateOne(obj.filter, obj.update, client, databaseName, obj.collectionName, bulkWriter);
			else if (message.tag==INSERT_ONE) {
				if (obj.filter!=null) {
					if (!hasOne(obj.filter, client, databaseName, obj.collectionName))
						insertOne(obj.value, client, databaseName, obj.collectionName, bulkWriter);
				}
				else
					insertOne(obj.value, client, databaseName, obj.collectionName, bulkWriter);
			}
			else if (message.tag==HAS_ONE) {
				obj.reserved = hasOne(obj.filter, client, databaseName, obj.collectionName);
				tell(obj, FIND_ONE, message.source);
			}
			else if (message.tag==FLUSH && bulkWrite)
				bulkWriter.flush();
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
