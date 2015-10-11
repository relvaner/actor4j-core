/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

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
