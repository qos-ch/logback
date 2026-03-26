/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import ch.qos.logback.classic.net.testObjectBuilders.LoggingEventBuilderInContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ceki G&uuml;lc&uuml;
 * @author Torsten Juergeleit
 */
public class AsyncAppenderTest {

    String thisClassName = this.getClass().getName();
    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    AsyncAppender asyncAppender = new AsyncAppender();
    ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();
    OnConsoleStatusListener onConsoleStatusListener = new OnConsoleStatusListener();
    LoggingEventBuilderInContext builder = new LoggingEventBuilderInContext(loggerContext, thisClassName,
            UnsynchronizedAppenderBase.class.getName());
    int diff = RandomUtil.getPositiveInt();

    @BeforeEach
    public void setUp() {
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        onConsoleStatusListener.setContext(loggerContext);
        loggerContext.getStatusManager().add(onConsoleStatusListener);
        onConsoleStatusListener.start();

        asyncAppender.setContext(loggerContext);
        listAppender.setContext(loggerContext);
        listAppender.setName("list");
        listAppender.start();
    }

    @Test
    public void eventWasPreparedForDeferredProcessing() {
        asyncAppender.addAppender(listAppender);
        asyncAppender.start();

        String k = "k" + diff;
        logbackMDCAdapter.put(k, "v");
        asyncAppender.doAppend(builder.build(diff));
        MDC.clear();

        asyncAppender.stop();
        assertFalse(asyncAppender.isStarted());

        // check the event
        assertEquals(1, listAppender.list.size());
        ILoggingEvent e = listAppender.list.get(0);

        // check that MDC values were correctly retained
        assertEquals("v", e.getMDCPropertyMap().get(k));
        assertFalse(e.hasCallerData());
    }

    @Test
    public void settingIncludeCallerDataPropertyCausedCallerDataToBeIncluded() {
        asyncAppender.addAppender(listAppender);
        asyncAppender.setIncludeCallerData(true);
        asyncAppender.start();

        asyncAppender.doAppend(builder.build(diff));
        asyncAppender.stop();

        // check the event
        assertEquals(1, listAppender.list.size());
        ILoggingEvent e = listAppender.list.get(0);
        assertTrue(e.hasCallerData());
        StackTraceElement ste = e.getCallerData()[0];
        assertEquals(thisClassName, ste.getClassName());
    }

    /**
     * LOGBACK-1469: an {@link AsyncAppender} with a delegate that is never attached to any logger
     * must still be stopped on {@link LoggerContext#reset()} (via {@code Context} life-cycle
     * registration), not only via {@code Logger.recursiveReset()}.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void orphanAsyncAppenderShouldStopWhenContextResets() {
        LoggerContext ctx = new LoggerContext();
        ctx.setMDCAdapter(logbackMDCAdapter);
        AsyncAppender async = new AsyncAppender();
        ListAppender<ILoggingEvent> delegate = new ListAppender<>();
        delegate.setContext(ctx);
        delegate.setName("list");
        delegate.start();
        async.setContext(ctx);
        async.setName("orphan-async");
        async.addAppender(delegate);
        async.start();

        assertTrue(async.isStarted());
        ctx.reset();

        assertFalse(async.isStarted(), "orphan AsyncAppender should be stopped when the context is reset");
        assertFalse(delegate.isStarted(), "nested appender should be stopped when AsyncAppender stops");
    }

    /**
     * Contrast to {@link #orphanAsyncAppenderShouldStopWhenContextResets()}: when the
     * async appender is attached to a logger, reset reaches it via {@code Logger.recursiveReset()}.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void asyncAppenderAttachedToLoggerIsStoppedWhenContextResets() {
        asyncAppender.setName("async-with-logger");
        asyncAppender.addAppender(listAppender);
        asyncAppender.start();

        Logger root = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.addAppender(asyncAppender);

        assertTrue(asyncAppender.isStarted());
        loggerContext.reset();

        assertFalse(asyncAppender.isStarted(), "AsyncAppender attached to a logger should be stopped on context reset");
        assertFalse(listAppender.isStarted(), "nested appender should be stopped when AsyncAppender stops");
    }
}
