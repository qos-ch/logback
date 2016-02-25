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
package ch.qos.logback.classic.net.testObjectBuilders;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LoggingEventBuilderInContext implements Builder<ILoggingEvent> {

    LoggerContext loggerContext;
    Logger logger;
    String fqcn;

    public LoggingEventBuilderInContext(LoggerContext loggerContext, String loggerName, String fqcn) {
        this.loggerContext = loggerContext;
        logger = loggerContext.getLogger(loggerName);
        this.fqcn = fqcn;
    }

    public ILoggingEvent build(int i) {
        LoggingEvent le = new LoggingEvent(fqcn, logger, Level.DEBUG, "hello " + i, null, null);
        return le;
    }

}
