/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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
package io.actor4j.core.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class ActorLogger {
	protected static volatile ActorLogger actorLogger;
	protected static final Object lock = new Object();
	
	protected final LoggerContext loggerContext;
	protected final LoggerConfig loggerConfig;
	
	public static final String LOGGER_NAME = "actor4j-logger";
	public static final String CONSOLE_APPENDER_NAME = "actor4j-console-appender";
	protected final String LAYOUT_CONSOLE = "%d{yyyy-MM-dd HH:mm:ss.SSS}\\t%-5p %m%n";
	
	// @See: https://logging.apache.org/log4j/2.x/manual/customconfig.html
	private ActorLogger() {
		loggerContext = (LoggerContext) LogManager.getContext(LogManager.class.getClassLoader(), false);
		Configuration config = loggerContext.getConfiguration();

		Appender consoleAppender = ConsoleAppender.newBuilder()
				.setName(CONSOLE_APPENDER_NAME)
				.setLayout(PatternLayout.newBuilder().withPattern(LAYOUT_CONSOLE).build())
				.setConfiguration(config)
				.build();
		consoleAppender.start();
	    AppenderRef[] appenderRefs = new AppenderRef[]{AppenderRef.createAppenderRef(CONSOLE_APPENDER_NAME, null, null)};
	    loggerConfig = LoggerConfig.createLogger(false, Level.DEBUG, LOGGER_NAME, "true", appenderRefs, null, config, null);
	    loggerConfig.addAppender(consoleAppender, null, null);

	    config.addAppender(consoleAppender);
	    config.addLogger(LOGGER_NAME, loggerConfig);
		loggerContext.updateLoggers(config);
	}
	
	public static void init() {
		// uses Double-Check-Idiom a la Bloch
		Object temp = actorLogger;
		if (temp==null) {
			synchronized (lock) {
				temp = actorLogger;
				if (temp==null) {
					actorLogger = new ActorLogger();
				}
			}
		}
	}
	
	public static Logger logger() {
		return actorLogger.loggerContext.getLogger(LOGGER_NAME);
	}
	
	public static void setLevel(Level level) { 
		actorLogger.loggerConfig.setLevel(level);
		actorLogger.loggerContext.updateLoggers();
	}
}
