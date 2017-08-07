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
package actor4j.core.utils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class ActorLogger {
	static ActorLogger actorLogger;
	
	protected Logger logger;
	
	protected final String layout_console = "%d{yyyy-MM-dd hh:mm:ss,SSS}\t%-5p %m%n";
	
	static {
		actorLogger = new ActorLogger();
	}
	
	private ActorLogger() {
		logger = Logger.getLogger(this.getClass());
		logger.addAppender(new ConsoleAppender(new PatternLayout(layout_console)));
	}
	
	public static Logger logger() {
		return actorLogger.logger;
	}
}
