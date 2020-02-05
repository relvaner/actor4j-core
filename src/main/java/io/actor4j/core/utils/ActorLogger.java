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
	private static volatile ActorLogger instance;
	private static final Object lock = new Object();
	
	protected final LoggerContext loggerContext;
	protected final LoggerConfig systemLoggerConfig;
	protected final LoggerConfig userLoggerConfig;
	
	public static final String SYSTEM_LOGGER_NAME = "actor4j-system-logger";
	public static final String USER_LOGGER_NAME = "actor4j-user-logger";
	public static final String SYSTEM_CONSOLE_APPENDER_NAME = "actor4j-system-console-appender";
	public static final String USER_CONSOLE_APPENDER_NAME = "actor4j-user-console-appender";
	
	protected final String LAYOUT_CONSOLE_SYSTEM = "[%-5p] %d{yyyy-MM-dd HH:mm:ss.SSS} [SYSTEM] [%t] %-40C -> %L %m%n";
	protected final String LAYOUT_CONSOLE_USER   = "[%-5p] %d{yyyy-MM-dd HH:mm:ss.SSS} [USER  ] [%t] %-40C -> %L [MESSAGE] %m%n";
	
	// @See: https://logging.apache.org/log4j/2.x/manual/customconfig.html
	private ActorLogger() {
		loggerContext = (LoggerContext) LogManager.getContext(LogManager.class.getClassLoader(), false);
		Configuration config = loggerContext.getConfiguration();

		Appender systemConsoleAppender = ConsoleAppender.newBuilder()
			.setName(SYSTEM_CONSOLE_APPENDER_NAME)
			.setLayout(PatternLayout.newBuilder().withPattern(LAYOUT_CONSOLE_SYSTEM).build())
			.setConfiguration(config)
			.build();
		systemConsoleAppender.start();
	    AppenderRef[] systemAppenderRefs = new AppenderRef[]{AppenderRef.createAppenderRef(SYSTEM_CONSOLE_APPENDER_NAME, null, null)};
	    systemLoggerConfig = LoggerConfig.createLogger(false, Level.DEBUG, SYSTEM_LOGGER_NAME, "true", systemAppenderRefs, null, config, null);
	    systemLoggerConfig.addAppender(systemConsoleAppender, null, null);
	    
	    Appender userConsoleAppender = ConsoleAppender.newBuilder()
			.setName(USER_CONSOLE_APPENDER_NAME)
			.setLayout(PatternLayout.newBuilder().withPattern(LAYOUT_CONSOLE_USER).build())
			.setConfiguration(config)
			.build();
		userConsoleAppender.start();
		AppenderRef[] userAppenderRefs = new AppenderRef[]{AppenderRef.createAppenderRef(USER_CONSOLE_APPENDER_NAME, null, null)};
		userLoggerConfig = LoggerConfig.createLogger(false, Level.DEBUG, USER_LOGGER_NAME, "true", userAppenderRefs, null, config, null);
		userLoggerConfig.addAppender(userConsoleAppender, null, null);

	    config.addAppender(systemConsoleAppender);
	    config.addAppender(userConsoleAppender);
	    config.addLogger(SYSTEM_LOGGER_NAME, systemLoggerConfig);
	    config.addLogger(USER_LOGGER_NAME, userLoggerConfig);
		loggerContext.updateLoggers(config);
	}
	
	protected static ActorLogger getInstance() {
		// uses Double-Check-Idiom a la Bloch
		ActorLogger result = instance;
		if (result==null) {
			synchronized (lock) {
				result = instance;
				if (result==null) {
					instance = result = new ActorLogger();
				}
			}
		}
		
		return result;
	}
	
	public static Logger systemLogger() {
		return getInstance().loggerContext.getLogger(SYSTEM_LOGGER_NAME);
	}
	
	public static Logger logger() {
		return getInstance().loggerContext.getLogger(USER_LOGGER_NAME);
	}

	public static void setLevel(Level level) { 
		getInstance().systemLoggerConfig.setLevel(level);
		getInstance().userLoggerConfig.setLevel(level);
		getInstance().loggerContext.updateLoggers();
	}
}
