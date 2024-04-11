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
package ch.qos.logback.classic.pattern;

import org.junit.jupiter.api.BeforeEach;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

public class EnsureExceptionHandlingTest {

    private PatternLayout pl = new PatternLayout();
    private LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(this.getClass());

    static final String XTH = "xth";
    static final String XCC = "xcc";

    @BeforeEach
    public void setUp() {
        pl.setContext(lc);
        pl.getInstanceConverterMap().put(XTH, XThrowableHandlingConverter.class.getName());
        pl.getInstanceConverterMap().put(XCC, XCompositeConverter.class.getName());
    }

    ILoggingEvent makeLoggingEvent(String msg, Exception ex) {
        return new LoggingEvent(EnsureExceptionHandlingTest.class.getName(), logger, Level.INFO, msg, ex, null);
    }

    @Test
    public void smoke() {
        pl.setPattern("%m %" + XTH + ")");
        pl.start();
        ILoggingEvent le = makeLoggingEvent("assert", null);
        pl.doLayout(le);
    }

    @Test
    public void withinComposite() {
        pl.setPattern("%m %" + XCC + "(%" + XTH + ")");
        pl.start();
        ILoggingEvent le = makeLoggingEvent("assert", null);
        pl.doLayout(le);
    }

}
