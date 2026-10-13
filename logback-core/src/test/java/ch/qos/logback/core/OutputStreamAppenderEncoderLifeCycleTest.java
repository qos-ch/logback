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
package ch.qos.logback.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import ch.qos.logback.core.testUtil.DummyEncoder;
import ch.qos.logback.core.status.testUtil.StatusChecker;

/**
 * Verifies that an {@link OutputStreamAppender} propagates its own life cycle to its encoder.
 *
 * <p>An encoder may hold resources (e.g. per-thread buffers) released only in
 * {@link ch.qos.logback.core.spi.LifeCycle#stop() stop()}; if the appender does not stop it, every
 * reconfiguration leaks the orphaned encoder's resources. Each test uses a {@link DummyEncoder}.</p>
 */
public class OutputStreamAppenderEncoderLifeCycleTest {

    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);

    /** 
     * Stopping the appender stops a running encoder. 
     */
    @Test
    public void stopStopsEncoder() {
        DummyEncoder<Object> encoder = newEncoder();
        OutputStreamAppender<Object> appender = newAppender(encoder);

        appender.start();
        assertTrue(encoder.isStarted());
        appender.doAppend("a");
        appender.stop();

        assertFalse(appender.isStarted());
        assertFalse(encoder.isStarted(), "encoder must be stopped when its appender is stopped");
        statusChecker.assertIsWarningOrErrorFree();
    }

    /** 
     * Stopping the appender is a no-op on an already stopped encoder (no error/warning). 
     */
    @Test
    public void stopDoesNotFailWhenEncoderAlreadyStopped() {
        DummyEncoder<Object> encoder = newEncoder();
        OutputStreamAppender<Object> appender = newAppender(encoder);

        appender.start();
        encoder.stop();
        appender.stop();

        assertFalse(appender.isStarted());
        assertFalse(encoder.isStarted());
        statusChecker.assertIsWarningOrErrorFree();
    }

    private DummyEncoder<Object> newEncoder() {
        DummyEncoder<Object> encoder = new DummyEncoder<>();
        encoder.setContext(context);
        encoder.start();
        return encoder;
    }

    private OutputStreamAppender<Object> newAppender(DummyEncoder<Object> encoder) {
        OutputStreamAppender<Object> appender = new OutputStreamAppender<>();
        appender.setContext(context);
        appender.setName("OSA");
        appender.setEncoder(encoder);
        appender.setOutputStream(new ByteArrayOutputStream());
        return appender;
    }
}
