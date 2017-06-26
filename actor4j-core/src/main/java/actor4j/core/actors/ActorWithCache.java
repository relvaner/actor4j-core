/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.actors;

import actor4j.core.messages.ActorMessage;
import actor4j.utils.Cache;
import actor4j.utils.CacheLRUWithGC;

public class ActorWithCache<K, V> extends Actor {
	protected int cacheSize;
	protected Cache<K, V> cache;
	
	public static final int GC     = 300;
	public static final int EVICT  = GC;
	public static final int GET    = 301;
	public static final int SET    = 302;
	public static final int UPDATE = 303;
	
	public ActorWithCache(String name, int cacheSize) {
		super(name);
		
		cache = new CacheLRUWithGC<>(cacheSize);
	}
	
	public ActorWithCache(int cacheSize) {
		this(null, cacheSize);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null && message.tag==GC)
			cache.gc(message.valueAsLong());
	}
}
