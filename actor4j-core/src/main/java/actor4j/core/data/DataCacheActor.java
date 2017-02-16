/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.data;

import actor4j.core.actors.ActorWithCache;
import actor4j.core.messages.ActorMessage;

import static actor4j.core.data.DataAccessActor.*;

import java.util.UUID;

public class DataCacheActor<K, V> extends ActorWithCache<K, V> {
	protected UUID dataAcess;
	
	public DataCacheActor(String name, int cacheSize, UUID dataAcess) {
		super(name, cacheSize);
		this.dataAcess = dataAcess;
	}
	
	public DataCacheActor(int cacheSize, UUID dataAcess) {
		this(null, cacheSize, dataAcess);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null && message.value instanceof DataAccessObject) {
			if (message.tag==GET)
				tell(message.value, GET, dataAcess);
			else if (message.tag==SET)
				tell(message.value, SET, dataAcess);
			else if (message.tag==UPDATE)
				tell(message.value, UPDATE, dataAcess);
			else if (message.tag==FIND_ONE) {
				@SuppressWarnings("unchecked")
				DataAccessObject<K,V> obj = (DataAccessObject<K,V>)message.value;
				cache.put(obj.key, obj.value);
				tell(obj, GET, obj.source);
			}
			else if (message.tag==GC)
				cache.gc(message.valueAsLong());
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
