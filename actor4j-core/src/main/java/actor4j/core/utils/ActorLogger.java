/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
