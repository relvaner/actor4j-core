/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.data;

import com.mongodb.MongoClient;

import actor4j.core.actors.ResourceActor;
import actor4j.core.messages.ActorMessage;

import static actor4j.core.actors.ActorWithCache.*;
import static actor4j.core.data.MongoUtils.*;

public class DataAccessActor<K, V> extends ResourceActor {
	protected MongoClient client;
	protected String databaseName;
	protected Class<V> valueType;
	
	public static final int HAS_ONE     = 304;
	public static final int INSERT_ONE  = 305;
	public static final int REPLACE_ONE = 306;
	public static final int UPDATE_ONE  = 307;
	public static final int FIND_ONE    = 308;
	
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
				obj.value = findOne(obj.filter, client, databaseName, obj.collectionName, valueType);
				tell(obj, FIND_ONE, message.source);
			}
			else if (message.tag==SET) {
				if (!hasOne(obj.filter, client, databaseName, obj.collectionName))
					insertOne(obj.value, client, databaseName, obj.collectionName);
				else
					replaceOne(obj.filter, obj.value, client, databaseName, obj.collectionName);
			}
			else if (message.tag==UPDATE_ONE || message.tag==UPDATE)
				updateOne(obj.filter, obj.update, client, databaseName, obj.collectionName);
			else if (message.tag==INSERT_ONE)
				insertOne(obj.value, client, databaseName, obj.collectionName);
			else if (message.tag==HAS_ONE) {
				obj.reserved = hasOne(obj.filter, client, databaseName, obj.collectionName);
				tell(obj, FIND_ONE, message.source);
			}
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
