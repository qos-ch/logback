/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic;


import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import ch.qos.logback.classic.pattern.ConverterTest;
import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.pattern.DynamicConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FluentAPILocationExtractionTest {
    static public class WithLocationInfoListAppender extends AppenderBase<ILoggingEvent> {

        DynamicConverter<ILoggingEvent> converter = new LineOfCallerConverter();
        public List<String> list = new ArrayList<>();

        protected void append(ILoggingEvent e) {
            String val = converter.convert(e);
            list.add(val);
        }
    }

    LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(ConverterTest.class);
    WithLocationInfoListAppender wlila = new WithLocationInfoListAppender();

    @BeforeEach
    public void setUp() {
        wlila.setContext(lc);
        wlila.start();

        logger.addAppender(wlila);
    }

    @Test
    public void smoke() {
        logger.addAppender(wlila);
        // line number to retain is the next line's number
        logger.atInfo().log("smoke");
        
        assertEquals(1, wlila.list.size());
        String result = wlila.list.get(0);
        assertEquals("59", result);
    }

}
