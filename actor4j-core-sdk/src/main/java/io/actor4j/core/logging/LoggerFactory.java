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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static io.actor4j.core.logging.ActorLogger.*;

public class LoggerFactory {
	protected static final Map<Level, String> LEVEL_AS_STRING;
	protected static volatile boolean simpleClassName;

	protected final Logger logger;
	protected final String name;
	
	static {
		LEVEL_AS_STRING = new HashMap<>();
		
		LEVEL_AS_STRING.put(ERROR, "ERROR");
		LEVEL_AS_STRING.put(WARN, "WARN");
		LEVEL_AS_STRING.put(INFO, "INFO");
		LEVEL_AS_STRING.put(DEBUG, "DEBUG");
		LEVEL_AS_STRING.put(TRACE, "TRACE");
	}
	
	public LoggerFactory(String name, Level level) {
		this(name, level, new Formatter () {
			@Override
			public String format(LogRecord record) {
				return LoggerFactory.format(record);
			}
		});
	}
	
	public LoggerFactory(String name, Level level, Formatter formatter) {
		super();
		this.name = name;

		logger = java.util.logging.Logger.getLogger(name);
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		handler.setFormatter(formatter);
		logger.addHandler(handler);
		logger.setUseParentHandlers(false);
		logger.setLevel(level);
	}

	public static Logger create(String name, Level level) {
		return new LoggerFactory(name, level).logger;
	}
	
	public static Logger create(String name, Level level, Formatter formatter) {
		return new LoggerFactory(name, level, formatter).logger;
	}
	
	protected static String format(LogRecord record) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
		String dateAsString = dateFormat.format(date);
		
		return String.format("[%-5s] %s [%s] [%s] %s::%s [MESSAGE] %s%n", 
				LEVEL_AS_STRING.get(record.getLevel()), 
				dateAsString, record.getLoggerName(), 
				Thread.currentThread().getName(), 
				simpleClassName ? getSimpleClassName(record.getSourceClassName()) : record.getSourceClassName(), 
				record.getSourceMethodName(),
				record.getMessage());
	}
	
	protected static String getSimpleClassName(String sourceClassName) {
		int index = sourceClassName.lastIndexOf(".");
		
		return sourceClassName.substring(index!=-1 ? index+1 : 0);
	}
	
	protected static void printLog(Level level, String msg, String loggerName) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
		String dateAsString = dateFormat.format(date);
		
		System.out.printf("[%-5s] %s [%s] [%s] [MESSAGE] %s%n", 
			LEVEL_AS_STRING.get(level), 
			dateAsString, loggerName, 
			Thread.currentThread().getName(),
			msg
		);
	}
}
