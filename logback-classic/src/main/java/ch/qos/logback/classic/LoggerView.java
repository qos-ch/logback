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

/**
 * An interface that allows Logger objects and LoggerSer objects to be used the
 * same way be client of the LoggingEvent object.
 * 
 * @author S&eacute;bastien Pennec
 */
public interface LoggerView {

	public String getName();

	public LoggerContextView getLoggerContext();

	public LoggerSer getLoggerSer();
}
