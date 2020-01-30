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
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class ActorLogger {
	static ActorLogger actorLogger;
	
	protected final LoggerContext loggerContext;
	protected final LoggerConfig loggerConfig;
	protected final String LAYOUT_PATTERN_CONSOLE = "%d{yyyy-MM-dd hh:mm:ss,SSS}\t%-5p %m%n";
	
	static {
		actorLogger = new ActorLogger();
	}
	
	// @See: https://logging.apache.org/log4j/2.x/manual/customconfig.html
	private ActorLogger() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setStatusLevel(Level.ERROR);
		builder.setConfigurationName("actor4j");
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
			.addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder.add(builder.newLayout("PatternLayout")
			.addAttribute("pattern", LAYOUT_PATTERN_CONSOLE));
		appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
			.addAttribute("marker", "FLOW"));
		builder.add(appenderBuilder);
		builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
			.add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
		builder.add(builder.newRootLogger(Level.DEBUG).add(builder.newAppenderRef("Stdout")));
		loggerContext = Configurator.initialize(builder.build());
		
		loggerConfig = loggerContext.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
	}
	
	public static Logger logger() {
		return actorLogger.loggerContext.getRootLogger();
	}
	
	public static void setLevel(Level level) { 
		actorLogger.loggerConfig.setLevel(level);
		actorLogger.loggerContext.updateLoggers();
	}
}
