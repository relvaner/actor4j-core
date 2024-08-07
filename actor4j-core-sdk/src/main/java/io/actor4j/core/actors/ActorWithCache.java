/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
package io.actor4j.core.actors;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.Cache;

import static io.actor4j.core.messages.ActorReservedTag.*;

public abstract class ActorWithCache<K, V> extends Actor {
	protected int cacheSize;
	protected Cache<K, V> cache;

	public static final int EVICT   = RESERVED_CACHE_EVICT;
	public static final int GET     = RESERVED_CACHE_GET;
	public static final int SET     = RESERVED_CACHE_SET;
	public static final int UPDATE  = RESERVED_CACHE_UPDATE;
	public static final int DEL     = RESERVED_CACHE_DEL;
	public static final int DEL_ALL = RESERVED_CACHE_DEL_ALL;
	public static final int CLEAR   = RESERVED_CACHE_CLEAR;
	public static final int CAS     = RESERVED_CACHE_CAS; // CompareAndSet
	public static final int CAU     = RESERVED_CACHE_CAU; // CompareAndUpdate
	
	public static final int SUCCESS = RESERVED_CACHE_SUCCESS;
	public static final int FAILURE = RESERVED_CACHE_FAILURE;
	
	public static final int SUBSCRIBE_SECONDARY = RESERVED_CACHE_SUBSCRIBE_SECONDARY;
	
	public static final int SYNC_WITH_STORAGE = RESERVED_CACHE_SYNC_WITH_STORAGE;
	
	public ActorWithCache(String name, int cacheSize) {
		super(name);
		
		this.cacheSize = cacheSize;
		cache = createCache(cacheSize);
	}
	
	public ActorWithCache(int cacheSize) {
		this(null, cacheSize);
	}
	
	public abstract Cache<K, V> createCache(int cacheSize);
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value()!=null && message.tag()==EVICT)
			cache.evict(message.valueAsLong());
	}
}
