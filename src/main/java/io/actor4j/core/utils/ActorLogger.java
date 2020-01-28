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
	static ActorLogger actorLogger;
	
	protected Logger logger;
	
	public static final String LOGGER_NAME = "actor4j-logger";
	public static final String CONSOLE_APPENDER_NAME = "actor4j-console-appender";
	protected final String LAYOUT_CONSOLE = "%d{yyyy-MM-dd hh:mm:ss,SSS}\t%-5p %m%n";
	
	static {
		actorLogger = new ActorLogger();
	}
	
	// @See: https://logging.apache.org/log4j/2.x/manual/customconfig.html
	private ActorLogger() {
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext();
		Configuration config = loggerContext.getConfiguration();

		Appender consoleAppender = ConsoleAppender.newBuilder()
				.setName(CONSOLE_APPENDER_NAME)
				.setLayout(PatternLayout.newBuilder().withPattern(LAYOUT_CONSOLE).build())
				.setConfiguration(loggerContext.getConfiguration())
				.build();
		consoleAppender.start();
	    AppenderRef[] appenderRefs = new AppenderRef[]{AppenderRef.createAppenderRef(CONSOLE_APPENDER_NAME, null, null)};
	    LoggerConfig loggerConfig = LoggerConfig.createLogger(false, null, LOGGER_NAME, "true", appenderRefs, null, config, null);
	    loggerConfig.addAppender(consoleAppender, null, null);
		
	    config.addAppender(consoleAppender);
	    config.addLogger(LOGGER_NAME, loggerConfig);
		loggerContext.updateLoggers(config);
		
		logger = LogManager.getContext().getLogger(LOGGER_NAME); 
	}
	
	public static Logger logger() {
		return actorLogger.logger;
	}
}
