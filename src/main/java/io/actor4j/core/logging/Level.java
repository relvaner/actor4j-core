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
package io.actor4j.core.logging;

import java.util.HashMap;
import java.util.Map;

public class Level {
	public static final Map<Integer, String> LEVEL_AS_STRING;
	
	public static final int ERROR = 1000;
	public static final int WARN  = 900;
	public static final int INFO  = 800;
	public static final int DEBUG = 700;
	public static final int TRACE = 500;
	
	static {
		LEVEL_AS_STRING = new HashMap<>();
		
		LEVEL_AS_STRING.put(ERROR, "ERROR");
		LEVEL_AS_STRING.put(WARN, "WARN");
		LEVEL_AS_STRING.put(INFO, "INFO");
		LEVEL_AS_STRING.put(DEBUG, "DEBUG");
		LEVEL_AS_STRING.put(TRACE, "TRACE");
	}
}
