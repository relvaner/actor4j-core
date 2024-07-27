/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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
package io.actor4j.core.messages;

import static io.actor4j.core.logging.ActorLogger.ERROR;
import static io.actor4j.core.logging.ActorLogger.systemLogger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ActorReservedTag {
	private static final Set<Integer> actorTags = ConcurrentHashMap.newKeySet();
	
	public static final int RESERVED_UP    	   = reservedTag(Integer.MAX_VALUE); // HEALTH_CHECK_SUCCESS
	public static final int RESERVED_TIMEOUT   = reservedTag(Integer.MAX_VALUE-1);
	
	public static final int RESERVED_UNHANDLED = reservedTag(Integer.MAX_VALUE-2);

	public static final int RESERVED_PUBLISH_SERVICE   = reservedTag(Integer.MAX_VALUE-100);
	public static final int RESERVED_UNPUBLISH_SERVICE = reservedTag(Integer.MAX_VALUE-101);
	public static final int RESERVED_LOOKUP_SERVICES   = reservedTag(Integer.MAX_VALUE-102);
	public static final int RESERVED_LOOKUP_SERVICE    = reservedTag(Integer.MAX_VALUE-103);
	
	public static final int RESERVED_CACHE_EVICT   = reservedTag(Integer.MAX_VALUE-200);
	public static final int RESERVED_CACHE_GET     = reservedTag(Integer.MAX_VALUE-201);
	public static final int RESERVED_CACHE_SET     = reservedTag(Integer.MAX_VALUE-202);
	public static final int RESERVED_CACHE_UPDATE  = reservedTag(Integer.MAX_VALUE-203);
	public static final int RESERVED_CACHE_DEL     = reservedTag(Integer.MAX_VALUE-204);
	public static final int RESERVED_CACHE_DEL_ALL = reservedTag(Integer.MAX_VALUE-205);
	public static final int RESERVED_CACHE_CLEAR   = reservedTag(Integer.MAX_VALUE-206);
	public static final int RESERVED_CACHE_CAS     = reservedTag(Integer.MAX_VALUE-207); // CompareAndSet
	public static final int RESERVED_CACHE_CAU     = reservedTag(Integer.MAX_VALUE-208); // CompareAndUpdate
	public static final int RESERVED_CACHE_SUCCESS = reservedTag(Integer.MAX_VALUE-209);
	public static final int RESERVED_CACHE_FAILURE = reservedTag(Integer.MAX_VALUE-210);
	public static final int RESERVED_CACHE_SUBSCRIBE_SECONDARY = reservedTag(Integer.MAX_VALUE-211);
	
	public static final int RESERVED_DATA_ACCESS_HAS_ONE     = reservedTag(Integer.MAX_VALUE-300);
	public static final int RESERVED_DATA_ACCESS_INSERT_ONE  = reservedTag(Integer.MAX_VALUE-301);
	public static final int RESERVED_DATA_ACCESS_REPLACE_ONE = reservedTag(Integer.MAX_VALUE-302);
	public static final int RESERVED_DATA_ACCESS_UPDATE_ONE  = reservedTag(Integer.MAX_VALUE-303);
	public static final int RESERVED_DATA_ACCESS_DELETE_ONE  = reservedTag(Integer.MAX_VALUE-304);
	public static final int RESERVED_DATA_ACCESS_FIND_ONE    = reservedTag(Integer.MAX_VALUE-305);
	public static final int RESERVED_DATA_ACCESS_FLUSH       = reservedTag(Integer.MAX_VALUE-306);
	public static final int RESERVED_DATA_ACCESS_SUCCESS     = RESERVED_CACHE_SUCCESS;
	public static final int RESERVED_DATA_ACCESS_FAILURE     = RESERVED_CACHE_FAILURE;

	static {
		// PodStatus
		for (int i=200; i<600; i++)
			actorTags.add(i);
	}
	
	private static int reservedTag(int tag) {
		if (tag<0)
			systemLogger().log(ERROR, String.format("[FATAL] Reserved tags below zero are system tags: %s", tag));
		else if (!actorTags.add(tag))
			systemLogger().log(ERROR, String.format("Reserved tag already exists: %s", tag));
		
		return tag;
	}
	
	public static int checkTag(int tag) {
		if (tag<0)
			systemLogger().log(ERROR, String.format("[FATAL] Tags below zero are system tags: %s", tag));
		else if (!actorTags.add(tag))
			systemLogger().log(ERROR, String.format("Tag already exists: %s", tag));
		
		return tag;
	}
}
