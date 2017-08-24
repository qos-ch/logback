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
package ch.qos.logback.classic.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.net.SyslogConstants;

public class LevelToSyslogSeverityTest {

    @Test
    public void smoke() {

        assertEquals(SyslogConstants.DEBUG_SEVERITY, LevelToSyslogSeverity.convert(createEventOfLevel(Level.TRACE)));

        assertEquals(SyslogConstants.DEBUG_SEVERITY, LevelToSyslogSeverity.convert(createEventOfLevel(Level.DEBUG)));

        assertEquals(SyslogConstants.INFO_SEVERITY, LevelToSyslogSeverity.convert(createEventOfLevel(Level.INFO)));

        assertEquals(SyslogConstants.WARNING_SEVERITY, LevelToSyslogSeverity.convert(createEventOfLevel(Level.WARN)));

        assertEquals(SyslogConstants.ERROR_SEVERITY, LevelToSyslogSeverity.convert(createEventOfLevel(Level.ERROR)));

    }

    ILoggingEvent createEventOfLevel(Level level) {
        LoggingEvent event = new LoggingEvent();
        event.setLevel(level);
        return event;
    }

}
