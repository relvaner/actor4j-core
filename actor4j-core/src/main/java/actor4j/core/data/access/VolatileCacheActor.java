/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.data.access;

import actor4j.core.actors.ActorWithCache;
import actor4j.core.messages.ActorMessage;

import static actor4j.core.data.access.DataAccessActor.*;

public class VolatileCacheActor<K, V> extends ActorWithCache<K, V> {
	public VolatileCacheActor(String name, int cacheSize) {
		super(name, cacheSize);
	}
	
	public VolatileCacheActor(int cacheSize) {
		this(null, cacheSize);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null && message.value instanceof DataAccessObject) {
			@SuppressWarnings("unchecked")
			DataAccessObject<K,V> obj = (DataAccessObject<K,V>)message.value;
			
			if (message.tag==GET) {
				obj.value = cache.get(obj.key);
				tell(obj, GET, obj.source); // deep copy necessary of obj.value
			}
			else if (message.tag==SET)
				cache.put(obj.key, obj.value);
			else if (message.tag==UPDATE)
				; // empty
			else if (message.tag==FIND_ONE) {
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
