/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2021, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.spi;

import org.slf4j.spi.DefaultLoggingEventBuilder;

import ch.qos.logback.classic.Logger;

/**
 * A trivial implementation LoggingEventBuilder which builds on
 * DefaultLoggingEventBuilder.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class LogbackLoggingEventBuilder extends DefaultLoggingEventBuilder {

	LoggingEvent loggingEvent;

    public static final String FQCN = LogbackLoggingEventBuilder.class.getName();

	public LogbackLoggingEventBuilder(Logger logger, org.slf4j.event.Level level) {
		super(logger, level);
	}

	@Override
	protected void log(org.slf4j.event.LoggingEvent sle) {
		Logger logbackLogger = (Logger) this.logger;
		logbackLogger.log(sle);
	}

}
