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

import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.NopEncoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ConsoleAppender must not close its underlying stream on stop. The console
 * (and the process-wide Jansi AnsiPrintStream when {@code withJansi=true}) is
 * not owned by the appender; closing it can shut down the JVM's stdout file
 * descriptor (see LOGBACK-1759 / issue #1063).
 *
 * <p>Unlike {@code ConsoleAppenderTest}, this class does not redirect
 * {@code System.out}, so it is safe to run under Maven Surefire.
 */
public class ConsoleAppenderCloseTest {

    @Test
    public void stopFlushesButDoesNotCloseUnderlyingStream() {
        Context context = new ContextBase();
        ConsoleAppender<Object> ca = new ConsoleAppender<>();
        ca.setContext(context);
        ca.setEncoder(new NopEncoder<>());
        ca.start();

        TrackingOutputStream tracking = new TrackingOutputStream();
        // Replace ConsoleTarget stream with a tracking stream so we can observe close/flush.
        ca.setOutputStream(tracking);
        ca.doAppend(new Object());
        ca.stop();

        Assertions.assertFalse(tracking.closed.get(), "ConsoleAppender must not close a stream it does not own");
        Assertions.assertTrue(tracking.flushCount.get() > 0, "ConsoleAppender should flush on stop");
    }

    @Test
    public void stopDoesNotCloseStreamInstalledForJansiContract() {
        // Models the Jansi-2 path: appender holds a stream whose close() is destructive
        // (AnsiPrintStream closes FileDescriptor.out). We use a tracking stand-in so the
        // JVM stdout FD is never at risk during the unit test.
        Context context = new ContextBase();
        ConsoleAppender<Object> ca = new ConsoleAppender<>();
        ca.setContext(context);
        ca.setEncoder(new EchoEncoder<>());
        ca.start();

        TrackingOutputStream destructiveConsoleStream = new TrackingOutputStream();
        ca.setOutputStream(destructiveConsoleStream);
        ca.doAppend("x");
        ca.stop();

        Assertions.assertFalse(destructiveConsoleStream.closed.get());
        Assertions.assertTrue(destructiveConsoleStream.flushCount.get() > 0);
    }

    private static final class TrackingOutputStream extends OutputStream {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final AtomicInteger flushCount = new AtomicInteger();
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

        @Override
        public void write(int b) {
            buf.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            buf.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            flushCount.incrementAndGet();
        }

        @Override
        public void close() {
            closed.set(true);
        }
    }
}
