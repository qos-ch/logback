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
package ch.qos.logback.classic.encoder;

import static ch.qos.logback.core.CoreConstants.CODES_URL;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class LayoutInsteadOfEncoderTest {

    // TeztConstants.TEST_SRC_PREFIX + "input/joran/ignore.xml"
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext();

    @Before
    public void setUp() {
        jc.setContext(loggerContext);

    }

    // jc.doConfigure(TeztConstants.TEST_SRC_PREFIX + "input/joran/ignore.xml");

    @Test
    public void layoutInsteadOfEncoer() throws JoranException {
        jc.doConfigure(ClassicTestConstants.JORAN_INPUT_PREFIX + "compatibility/layoutInsteadOfEncoder.xml");
        StatusPrinter.print(loggerContext);
        StatusChecker checker = new StatusChecker(loggerContext);
        checker.assertContainsMatch(Status.WARN, "This appender no longer admits a layout as a sub-component");
        checker.assertContainsMatch(Status.WARN, "See also " + CODES_URL + "#layoutInsteadOfEncoder for details");

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        FileAppender<ILoggingEvent> fileAppender = (FileAppender<ILoggingEvent>) root.getAppender("LIOE");
        assertTrue(fileAppender.isStarted());
        assertTrue(fileAppender.getEncoder() instanceof LayoutWrappingEncoder);
    }
    
    @Test
    public void immediateFlushInEncoder_TRUE() throws JoranException {
        immediateFlushInEncoder(true);
    }
    
    @Test
    public void immediateFlushInEncoder_FALSE() throws JoranException {
        immediateFlushInEncoder(false);
    }
    
    public void immediateFlushInEncoder(Boolean immediateFlush) throws JoranException {
        loggerContext.putProperty("immediateFlush", immediateFlush.toString());
        jc.doConfigure(ClassicTestConstants.JORAN_INPUT_PREFIX + "compatibility/immediateFlushInEncoder.xml");
        StatusPrinter.print(loggerContext);
        StatusChecker checker = new StatusChecker(loggerContext);

        checker.assertContainsMatch(Status.WARN, "As of version 1.2.0 \"immediateFlush\" property should be set within the enclosing Appender.");
        checker.assertContainsMatch(Status.WARN, "Please move \"immediateFlush\" property into the enclosing appender.");
        checker.assertContainsMatch(Status.WARN, "Setting the \"immediateFlush\" property of the enclosing appender to "+immediateFlush);

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        FileAppender<ILoggingEvent> fileAppender = (FileAppender<ILoggingEvent>) root.getAppender("LIOE");
        assertTrue(fileAppender.isStarted());
        assertEquals(immediateFlush, Boolean.valueOf(fileAppender.isImmediateFlush())); 
    }

  
}
