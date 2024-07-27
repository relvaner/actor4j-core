/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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
package io.actor4j.core.pods.utils;

import java.util.HashMap;
import java.util.Map;
import static io.actor4j.core.messages.ActorReservedTag.*;

public class PodStatus {
	protected static final Map<Integer, String> statusMap;
	
	public static final int OFFSET = RESERVED_POD_STATUS_RANGE_START;
	
	public static final int OK 						= OFFSET+200; 
	public static final int CREATED 				= OFFSET+201; 
	public static final int ACCEPTED 				= OFFSET+202; 
	
	public static final int BAD_REQUEST 			= OFFSET+400;
	public static final int UNAUTHORIZED 			= OFFSET+401;
	public static final int FORBIDDED 				= OFFSET+403; 
	public static final int NOT_FOUND 				= OFFSET+404; 
	public static final int METHOD_NOT_ALLOWED 		= OFFSET+405;
	public static final int NOT_ACCEPTABALE 		= OFFSET+406;
	public static final int AUTHENTICATION_REQUIRED = OFFSET+407;
	public static final int CONFLICT 				= OFFSET+409;
	public static final int GONE 					= OFFSET+410;
	
	public static final int INTERNAL_SERVER_ERROR 	= OFFSET+500;
	public static final int NOT_IMPLEMENTED 		= OFFSET+501;
	public static final int SERVICE_UNAVAILABLE 	= OFFSET+503;
	public static final int LOOP_DETECTED 	        = OFFSET+508;
	
	static {
		statusMap = new HashMap<>();
		statusMap.put(PodStatus.OK, "Ok");
		statusMap.put(PodStatus.CREATED, "Created");
		statusMap.put(PodStatus.ACCEPTED, "Accepted");
		
		statusMap.put(PodStatus.BAD_REQUEST, "Bad Request");
		statusMap.put(PodStatus.UNAUTHORIZED, "Unauthorized");
		statusMap.put(PodStatus.FORBIDDED, "Forbidden");
		statusMap.put(PodStatus.NOT_FOUND, "Not Found");
		statusMap.put(PodStatus.METHOD_NOT_ALLOWED, "Method Not Allowed");
		statusMap.put(PodStatus.NOT_ACCEPTABALE, "Not Acceptable");
		statusMap.put(PodStatus.AUTHENTICATION_REQUIRED, "Authentication Required");
		statusMap.put(PodStatus.CONFLICT, "Conflict");
		statusMap.put(PodStatus.GONE, "Gone");
		
		statusMap.put(PodStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
		statusMap.put(PodStatus.NOT_IMPLEMENTED, "Not Implemented");
		statusMap.put(PodStatus.SERVICE_UNAVAILABLE, "Service Unavailable");
		statusMap.put(PodStatus.LOOP_DETECTED, "Loop Detected");
	}
	
	public static String getStatus(int code) {
		return statusMap.get(code);
	}
}
