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
package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.appender.AbstractAppenderTest;
import ch.qos.logback.core.encoder.DummyEncoder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class RollingFileAppenderTest extends AbstractAppenderTest<Object> {

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    Context context = new ContextBase();

    TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();
    int diff = RandomUtil.getPositiveInt();
    String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

    @Before
    public void setUp() throws Exception {
        // noStartTest fails if the context is set in setUp
        // rfa.setContext(context);

        rfa.setEncoder(new DummyEncoder<Object>());
        rfa.setName("test");
        tbrp.setContext(context);
        tbrp.setParent(rfa);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Override
    protected Appender<Object> getAppender() {
        return rfa;
    }

    @Override
    protected Appender<Object> getConfiguredAppender() {
        rfa.setContext(context);
        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);

        rfa.start();
        return rfa;
    }

    @Test
    public void testPrudentModeLogicalImplications() {
        rfa.setContext(context);
        // prudent mode will force "file" property to be null
        rfa.setFile("some non null value");
        rfa.setAppend(false);
        rfa.setPrudent(true);

        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);

        rfa.start();

        assertTrue(rfa.isAppend());
        assertNull(rfa.rawFileProperty());
        assertTrue(rfa.isStarted());
    }

    @Test
    public void testPrudentModeLogicalImplicationsOnCompression() {
        rfa.setContext(context);
        rfa.setAppend(false);
        rfa.setPrudent(true);

        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log.zip");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);

        rfa.start();

        StatusChecker checker = new StatusChecker(context);
        assertFalse(rfa.isStarted());
        assertEquals(Status.ERROR, checker.getHighestLevel(0));
    }

    @Test
    public void testFilePropertyAfterRollingPolicy() {
        rfa.setContext(context);
        rfa.setRollingPolicy(tbrp);
        rfa.setFile("x");
        StatusPrinter.print(context);
        StatusChecker statusChecker = new StatusChecker(context.getStatusManager());
        statusChecker.assertContainsMatch(Status.ERROR, "File property must be set before any triggeringPolicy ");
    }

    @Test
    public void testFilePropertyAfterTriggeringPolicy() {
        rfa.setContext(context);
        rfa.setTriggeringPolicy(new SizeBasedTriggeringPolicy<Object>());
        rfa.setFile("x");
        StatusChecker statusChecker = new StatusChecker(context.getStatusManager());
        statusChecker.assertContainsMatch(Status.ERROR, "File property must be set before any triggeringPolicy ");
    }

    @Test
    public void testFileNameWithParenthesis() {
        // if ')' is not escaped, the test throws
        // java.lang.IllegalStateException: FileNamePattern [.../program(x86)/toto-%d.log] does not contain a valid
        // DateToken
        rfa.setContext(context);
        tbrp.setFileNamePattern(randomOutputDir + "program(x86)/toto-%d.log");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);
        rfa.start();
        rfa.doAppend("hello");
    }

    @Test
    public void stopTimeBasedRollingPolicy() {
        rfa.setContext(context);

        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log.zip");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);
        rfa.start();

        StatusPrinter.print(context);
        assertTrue(tbrp.isStarted());
        assertTrue(rfa.isStarted());
        rfa.stop();
        assertFalse(rfa.isStarted());
        assertFalse(tbrp.isStarted());

    }

    @Test
    public void stopFixedWindowRollingPolicy() {
        rfa.setContext(context);
        rfa.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-.log");

        FixedWindowRollingPolicy fwRollingPolicy = new FixedWindowRollingPolicy();
        fwRollingPolicy.setContext(context);
        fwRollingPolicy.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%i.log.zip");
        fwRollingPolicy.setParent(rfa);
        fwRollingPolicy.start();
        SizeBasedTriggeringPolicy<Object> sbTriggeringPolicy = new SizeBasedTriggeringPolicy<Object>();
        sbTriggeringPolicy.setContext(context);
        sbTriggeringPolicy.start();

        rfa.setRollingPolicy(fwRollingPolicy);
        rfa.setTriggeringPolicy(sbTriggeringPolicy);

        rfa.start();

        StatusPrinter.print(context);
        assertTrue(fwRollingPolicy.isStarted());
        assertTrue(sbTriggeringPolicy.isStarted());
        assertTrue(rfa.isStarted());
        rfa.stop();
        assertFalse(rfa.isStarted());
        assertFalse(fwRollingPolicy.isStarted());
        assertFalse(sbTriggeringPolicy.isStarted());

    }

    /**
     * Test for http://jira.qos.ch/browse/LOGBACK-796
     */
    @Test
    public void testFileShouldNotMatchFileNamePattern() {
        rfa.setContext(context);
        rfa.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX + "x-2013-04.log");
        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "x-%d{yyyy-MM}.log");
        tbrp.start();

        rfa.setRollingPolicy(tbrp);
        rfa.start();
        StatusChecker statusChecker = new StatusChecker(context);
        final String msg = "File property collides with fileNamePattern. Aborting.";
        boolean containsMatch = statusChecker.containsMatch(Status.ERROR, msg);
        assertTrue("Missing error: " + msg, containsMatch);
    }

    @Test
    public void collidingTimeformat() {
        rfa.setContext(context);
        rfa.setAppend(false);
        rfa.setPrudent(true);

        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d{dd}.log.zip");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);

        rfa.start();

        StatusChecker checker = new StatusChecker(context);
        assertFalse(rfa.isStarted());
        assertEquals(Status.ERROR, checker.getHighestLevel(0));
        StatusPrinter.print(context);
        checker.assertContainsMatch("The date format in FileNamePattern will result");
    }

    @Test
    public void collidingFileNamePattern() {
        String filenamePattern = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "-collision-%d.log.zip";

        RollingFileAppender<Object> appender0 = new RollingFileAppender<Object>();
        appender0.setName("FA0");
        appender0.setContext(context);
        appender0.setEncoder(new DummyEncoder<Object>());
        TimeBasedRollingPolicy<Object> tbrp0 = new TimeBasedRollingPolicy<Object>();
        tbrp0.setContext(context);
        tbrp0.setFileNamePattern(filenamePattern);
        tbrp0.setParent(appender0);
        tbrp0.start();
        appender0.setRollingPolicy(tbrp0);
        appender0.start();
        assertTrue(appender0.isStarted());

        RollingFileAppender<Object> appender1 = new RollingFileAppender<Object>();
        appender1.setName("FA1");
        appender1.setFile("X");
        appender1.setContext(context);
        appender1.setEncoder(new DummyEncoder<Object>());
        TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>();
        tbrp1.setContext(context);
        tbrp1.setFileNamePattern(filenamePattern);
        tbrp1.setParent(appender1);
        tbrp1.start();
        appender1.setRollingPolicy(tbrp1);
        appender1.start();

        // StatusPrinter.print(context);

        assertFalse(appender1.isStarted());
        StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch(Status.ERROR, "'FileNamePattern' option has the same value");
    }
    
}
