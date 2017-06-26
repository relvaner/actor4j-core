/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.data.access;

import actor4j.core.actors.ActorWithCache;
import actor4j.core.messages.ActorMessage;

import static actor4j.core.data.access.DataAccessActor.*;

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
			@SuppressWarnings("unchecked")
			DataAccessObject<K,V> obj = (DataAccessObject<K,V>)message.value;
			
			if (message.tag==GET) {
				obj.value = cache.get(obj.key);
				if (obj.value!=null)
					tell(obj, GET, obj.source); // deep copy necessary of obj.value
				else
					tell(message.value, GET, dataAcess);
			}
			else if (message.tag==SET) {
				cache.put(obj.key, obj.value);
				tell(message.value, SET, dataAcess);
			}
			else if (message.tag==UPDATE) {
				cache.remove(obj.key);
				tell(message.value, UPDATE, dataAcess);
			}
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
