/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubPatternLayoutTest {

    LoggerContext context = new LoggerContext();

    @Test public void smoke() {
        SubPatternLayout layout = new SubPatternLayout();
        layout.setPattern("%"+SubPatternLayout.DOOO);
        layout.setContext(context);
        layout.start();
        LoggingEvent event = new LoggingEvent();
        event.setTimeStamp(0);

        String result = layout.doLayout(event);
        assertEquals("1970-01-01 01:00:00,000", result);
    }
}
