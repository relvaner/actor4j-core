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
	
	public static final int RESERVED_RANGE_START = 10_000;
	public static final int RESERVED_RANGE_END   = 20_000;
	
	public static final int RESERVED_OFFSET_MIN  = RESERVED_RANGE_START;
	public static final int RESERVED_OFFSET_MAX  = RESERVED_RANGE_END;
	
	public static final int RESERVED_POD_STATUS_RANGE_START = RESERVED_OFFSET_MIN;
	public static final int RESERVED_POD_STATUS_RANGE_END   = RESERVED_OFFSET_MIN+600;
	
	public static final int RESERVED_POD_REQUEST_METHOD_RANGE_START = RESERVED_OFFSET_MIN+1000;
	public static final int RESERVED_POD_REQUEST_METHOD_RANGE_END   = RESERVED_OFFSET_MIN+1500;
	
	public static final int RESERVED_UP    	   = reservedTag(RESERVED_OFFSET_MAX); // HEALTH_CHECK_SUCCESS
	public static final int RESERVED_TIMEOUT   = reservedTag(RESERVED_OFFSET_MAX-1);
	
	public static final int RESERVED_UNHANDLED = reservedTag(RESERVED_OFFSET_MAX-2);

	public static final int RESERVED_PUBLISH_SERVICE   = reservedTag(RESERVED_OFFSET_MAX-100);
	public static final int RESERVED_UNPUBLISH_SERVICE = reservedTag(RESERVED_OFFSET_MAX-101);
	public static final int RESERVED_LOOKUP_SERVICES   = reservedTag(RESERVED_OFFSET_MAX-102);
	public static final int RESERVED_LOOKUP_SERVICE    = reservedTag(RESERVED_OFFSET_MAX-103);
	
	public static final int RESERVED_CACHE_EVICT   = reservedTag(RESERVED_OFFSET_MAX-200);
	public static final int RESERVED_CACHE_GET     = reservedTag(RESERVED_OFFSET_MAX-201);
	public static final int RESERVED_CACHE_SET     = reservedTag(RESERVED_OFFSET_MAX-202);
	public static final int RESERVED_CACHE_UPDATE  = reservedTag(RESERVED_OFFSET_MAX-203);
	public static final int RESERVED_CACHE_DEL     = reservedTag(RESERVED_OFFSET_MAX-204);
	public static final int RESERVED_CACHE_DEL_ALL = reservedTag(RESERVED_OFFSET_MAX-205);
	public static final int RESERVED_CACHE_CLEAR   = reservedTag(RESERVED_OFFSET_MAX-206);
	public static final int RESERVED_CACHE_CAS     = reservedTag(RESERVED_OFFSET_MAX-207); // CompareAndSet
	public static final int RESERVED_CACHE_CAU     = reservedTag(RESERVED_OFFSET_MAX-208); // CompareAndUpdate
	public static final int RESERVED_CACHE_SUCCESS = reservedTag(RESERVED_OFFSET_MAX-209);
	public static final int RESERVED_CACHE_FAILURE = reservedTag(RESERVED_OFFSET_MAX-210);
	public static final int RESERVED_CACHE_SUBSCRIBE_SECONDARY = reservedTag(RESERVED_OFFSET_MAX-211);
	
	public static final int RESERVED_DATA_ACCESS_HAS_ONE     = reservedTag(RESERVED_OFFSET_MAX-300);
	public static final int RESERVED_DATA_ACCESS_INSERT_ONE  = reservedTag(RESERVED_OFFSET_MAX-301);
	public static final int RESERVED_DATA_ACCESS_REPLACE_ONE = reservedTag(RESERVED_OFFSET_MAX-302);
	public static final int RESERVED_DATA_ACCESS_UPDATE_ONE  = reservedTag(RESERVED_OFFSET_MAX-303);
	public static final int RESERVED_DATA_ACCESS_DELETE_ONE  = reservedTag(RESERVED_OFFSET_MAX-304);
	public static final int RESERVED_DATA_ACCESS_FIND_ONE    = reservedTag(RESERVED_OFFSET_MAX-305);
	public static final int RESERVED_DATA_ACCESS_FLUSH       = reservedTag(RESERVED_OFFSET_MAX-306);
	public static final int RESERVED_DATA_ACCESS_SUCCESS     = RESERVED_CACHE_SUCCESS;
	public static final int RESERVED_DATA_ACCESS_FAILURE     = RESERVED_CACHE_FAILURE;

	static {
		// PodStatus
		for (int i=RESERVED_POD_STATUS_RANGE_START; i<RESERVED_POD_STATUS_RANGE_END; i++)
			actorTags.add(i);
		// PodRequestMethod
		for (int i=RESERVED_POD_REQUEST_METHOD_RANGE_START; i<RESERVED_POD_REQUEST_METHOD_RANGE_END; i++)
			actorTags.add(i);
	}
	
	private static int reservedTag(int tag) {
		if (tag<0)
			systemLogger().log(ERROR, String.format("[FATAL] Reserved tags below zero are system tags: %d", tag));
		else if (!actorTags.add(tag))
			systemLogger().log(ERROR, String.format("Reserved tag already exists: %d", tag));
		
		return tag;
	}
	
	public static int userTag(int tag) {
		if (tag<0)
			systemLogger().log(ERROR, String.format("[FATAL] Tags below zero are system tags: %d", tag));
		else if (tag>=RESERVED_RANGE_START && tag<=RESERVED_RANGE_END)
			systemLogger().log(ERROR, String.format("[FATAL] Tag within reserved range [%d, %d]: %d", 
				RESERVED_RANGE_START, RESERVED_RANGE_END, tag));
		else if (!actorTags.add(tag))
			systemLogger().log(ERROR, String.format("Tag already exists: %d", tag));
		
		return tag;
	}
}
