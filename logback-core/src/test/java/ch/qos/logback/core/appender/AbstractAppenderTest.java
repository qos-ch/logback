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
package ch.qos.logback.core.appender;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

abstract public class AbstractAppenderTest<E> {

    abstract protected Appender<E> getAppender();

    abstract protected Appender<E> getConfiguredAppender();

    Context context = new ContextBase();

    @Test
    public void testNewAppender() {
        // new appenders should be inactive
        Appender<E> appender = getAppender();
        assertFalse(appender.isStarted());
    }

    @Test
    public void testConfiguredAppender() {
        Appender<E> appender = getConfiguredAppender();
        appender.start();
        assertTrue(appender.isStarted());

        appender.stop();
        assertFalse(appender.isStarted());

    }

    @Test
    public void testNoStart() {
        Appender<E> appender = getAppender();
        appender.setContext(context);
        appender.setName("doh");
        // is null OK?
        appender.doAppend(null);
        StatusChecker checker = new StatusChecker(context.getStatusManager());
        StatusPrinter.print(context);
        checker.assertContainsMatch("Attempted to append to non started appender \\[doh\\].");
    }
}
