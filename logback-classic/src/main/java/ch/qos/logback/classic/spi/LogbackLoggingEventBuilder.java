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
 * A trivial implementation LoggingEventBuilder which build on DefaultLoggingEventBuilder.
 * 
 * @author Ceki Gulcu
 *
 */
public class LogbackLoggingEventBuilder extends DefaultLoggingEventBuilder {

	public LogbackLoggingEventBuilder(Logger logger, org.slf4j.event.Level level) {
		super(logger, level);
	}

}
