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

import static io.actor4j.core.logging.Level.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LoggerImpl implements Logger {
	protected java.util.logging.Logger logger;
	protected String name;
	
	public LoggerImpl(String name, int level) {
		super();
		this.name = name;
		
		logger = java.util.logging.Logger.getLogger(name);
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		handler.setFormatter(new Formatter () {
			@Override
			public String format(LogRecord record) {
				return LoggerImpl.this.format(record);
			}
		});
		logger.addHandler(handler);
		logger.setUseParentHandlers(false);
		setLevel(level);
	}
	
	protected String format(LogRecord record) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
		String dateAsString = dateFormat.format(date);  
		return String.format("[%-5s] %s [%s] [%s] [MESSAGE] %s%n", 
				LEVEL_AS_STRING.get(record.getLevel().intValue()), 
				dateAsString, 
				record.getLoggerName(), 
				Thread.currentThread().getName(),
				record.getMessage());
	}
	
	@Override
	public void setLevel(int level) {
		switch (level) {
			case ERROR: 
				logger.setLevel(java.util.logging.Level.SEVERE);
				break;
			case WARN:
				logger.setLevel(java.util.logging.Level.WARNING);
				break;
			case INFO:
				logger.setLevel(java.util.logging.Level.INFO);
				break;
			case DEBUG:
				logger.setLevel(java.util.logging.Level.CONFIG);
				break;
			case TRACE:
				logger.setLevel(java.util.logging.Level.FINE);
				break;
			default:
				logger.setLevel(java.util.logging.Level.CONFIG);
		}
	}
	
	@Override
	public void error(String msg) {
		logger.severe(msg);
	}
	
	@Override
	public void warn(String msg) {
		logger.warning(msg);
	}
	
	@Override
	public void info(String msg) {
		logger.info(msg);
	}
	
	@Override
	public void debug(String msg) {
		logger.config(msg);
		
	}
	
	@Override
	public void trace(String msg) {
		logger.fine(msg);
	}

	@Override
	public void error(String format, Object... arguments) {
		error(String.format(format, arguments));
	}

	@Override
	public void warn(String format, Object... arguments) {
		warn(String.format(format, arguments));
	}

	@Override
	public void info(String format, Object... arguments) {
		info(String.format(format, arguments));
	}

	@Override
	public void debug(String format, Object... arguments) {
		debug(String.format(format, arguments));
	}

	@Override
	public void trace(String format, Object... arguments) {
		trace(String.format(format, arguments));
	}
}
