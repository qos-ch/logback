/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.issue.logback474;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * An appender which calls logback recursively
 * 
 * @author Ralph Goers
 */

public class LoggingAppender extends AppenderBase<ILoggingEvent> {

    Logger logger;

    public void start() {
        super.start();
        logger = ((LoggerContext) getContext()).getLogger("Ignore");
    }

    protected void append(ILoggingEvent eventObject) {
        logger.debug("Ignore this");
    }
}
