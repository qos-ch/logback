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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
//import ch.qos.logback.core.util.StatusPrinter;

public class CompositeConverterTest {

    private PatternLayout pl = new PatternLayout();
    private LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(this.getClass());
    StatusChecker sc = new StatusChecker(lc);
    
    
    @BeforeEach
    public void setUp() {
        pl.setContext(lc);
    }

    ILoggingEvent makeLoggingEvent(String msg, Exception ex) {
        return new LoggingEvent(CompositeConverterTest.class.getName(), logger, Level.INFO, msg, ex, null);
    }

    

    @Test
    public void testLogback1582() {
        // EVAL_REF is searched within the context, if context is not set (== null), then
        // a NullPointerException will be thrown
        pl.setPattern("%m  %replace(%rootException{5, EVAL_REF}){'\\n', 'XYZ'}\"");
        pl.start();
        ILoggingEvent le = makeLoggingEvent("assert", new Exception("test"));
        
        String result = pl.doLayout(le);
        sc.assertIsErrorFree();
        assertTrue(result.contains("XYZ"));
    }

}
