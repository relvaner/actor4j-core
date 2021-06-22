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

public class PodStatus {
	private static final Map<Integer, String> statusMap;
	
	public static final int OK = 200; 
	public static final int CREATED = 201; 
	public static final int ACCEPTED = 202; 
	
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 401;
	public static final int FORBIDDED = 403; 
	public static final int NOT_FOUND = 404; 
	public static final int METHOD_NOT_ALLOWED = 405;
	public static final int NOT_ACCEPTABALE = 406;
	public static final int AUTHENTICATION_REQUIRED = 407;
	public static final int CONFLICT = 409;
	public static final int GONE = 410;
	
	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final int NOT_IMPLEMENTED = 501;
	public static final int SERVICE_UNAVAILABLE = 503;
	
	static {
		statusMap = new HashMap<>();
		statusMap.put(200, "Ok");
		statusMap.put(201, "Created");
		statusMap.put(202, "Accepted");
		
		statusMap.put(400, "Bad Request");
		statusMap.put(401, "Unauthorized");
		statusMap.put(403, "Forbidden");
		statusMap.put(404, "Not Found");
		statusMap.put(405, "Method Not Allowed");
		statusMap.put(406, "Not Acceptable");
		statusMap.put(407, "Authentication Required");
		statusMap.put(409, "Conflict");
		statusMap.put(409, "Gone");
		
		statusMap.put(500, "Internal Server Error");
		statusMap.put(501, "Not Implemented");
		statusMap.put(503, "Service Unavailable");
	}
	
	public static String getStatus(int code) {
		return statusMap.get(code);
	}
}
