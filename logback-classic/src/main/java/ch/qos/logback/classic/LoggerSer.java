/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import java.io.Serializable;

/**
 * The object used to contain the LoggerContext informations once the
 * serialization has taken place.
 * 
 * @author S&eacute;bastien Pennec
 */
public class LoggerSer implements LoggerView, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5323186084164784257L;

	Level level;
	LoggerContextView loggerContext;
	String name;

	public Level getLevel() {
		return level;
	}

	public LoggerContextView getLoggerContext() {
		return loggerContext;
	}

	public String getName() {
		return name;
	}

	public LoggerSer getLoggerSer() {
		return this;
	}
}
