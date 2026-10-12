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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

import ch.qos.logback.core.encoder.EncoderBase;
import ch.qos.logback.core.status.testUtil.StatusChecker;

/**
 * Stopping an {@link OutputStreamAppender} must also stop its encoder.
 *
 * <p>Encoders may hold resources that are only released in {@link ch.qos.logback.core.spi.LifeCycle#stop() stop()},
 * typically per-thread buffers (e.g. logstash-logback-encoder's {@code ThreadLocalHolder}). If the appender never
 * stops its encoder, every context reset (for example a {@code scan="true"} reconfiguration) orphans the previous
 * encoder together with one buffer per thread that ever logged through it, and the heap grows on every
 * reconfiguration.</p>
 */
public class OutputStreamAppenderEncoderLifeCycleTest {

    private static final int THREAD_COUNT = 16;
    private static final int RECONFIGURATIONS = 10;

    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);

    @Test
    public void stopStopsEncoder() {
        PerThreadBufferEncoder<Object> encoder = newEncoder();
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
     * Reproduces the leak: repeatedly replace (stop + start a new) appender, as a context reset/reconfiguration does,
     * while several threads log through each one. Without the fix, every old encoder keeps one buffer per thread.
     */
    @Test
    public void replacedAppendersDoNotRetainPerThreadBuffers() throws InterruptedException {
        List<PerThreadBufferEncoder<Object>> encoders = new ArrayList<>();

        for (int r = 0; r < RECONFIGURATIONS; r++) {
            PerThreadBufferEncoder<Object> encoder = newEncoder();
            OutputStreamAppender<Object> appender = newAppender(encoder);
            appender.start();
            encoders.add(encoder);

            logFromThreads(appender);
            assertEquals(THREAD_COUNT, encoder.retainedBufferCount());

            appender.stop();
        }

        int retained = 0;
        for (PerThreadBufferEncoder<Object> encoder : encoders) {
            retained += encoder.retainedBufferCount();
        }
        assertEquals(0, retained, "stopped appenders' encoders still retain per-thread buffers");
        statusChecker.assertIsWarningOrErrorFree();
    }

    @Test
    public void stopDoesNotFailWhenEncoderAlreadyStopped() {
        PerThreadBufferEncoder<Object> encoder = newEncoder();
        OutputStreamAppender<Object> appender = newAppender(encoder);

        appender.start();
        encoder.stop();
        appender.stop();

        assertFalse(appender.isStarted());
        assertFalse(encoder.isStarted());
        assertEquals(1, encoder.stopCount(), "an already stopped encoder must not be stopped again");
        statusChecker.assertIsWarningOrErrorFree();
    }

    private void logFromThreads(OutputStreamAppender<Object> appender) throws InterruptedException {
        List<Thread> threads = new ArrayList<>(THREAD_COUNT);
        for (int t = 0; t < THREAD_COUNT; t++) {
            Thread thread = new Thread(() -> appender.doAppend("x"), "logging-thread-" + t);
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.join(10_000);
            assertFalse(thread.isAlive(), "thread did not finish: " + thread.getName());
        }
    }

    private PerThreadBufferEncoder<Object> newEncoder() {
        PerThreadBufferEncoder<Object> encoder = new PerThreadBufferEncoder<>();
        encoder.setContext(context);
        encoder.start();
        return encoder;
    }

    private OutputStreamAppender<Object> newAppender(PerThreadBufferEncoder<Object> encoder) {
        OutputStreamAppender<Object> appender = new OutputStreamAppender<>();
        appender.setContext(context);
        appender.setName("OSA");
        appender.setEncoder(encoder);
        appender.setOutputStream(new ByteArrayOutputStream());
        return appender;
    }

    /**
     * Encoder that, like logstash-logback-encoder, keeps a reusable buffer per calling thread and only releases them
     * in {@link #stop()}.
     */
    static class PerThreadBufferEncoder<E> extends EncoderBase<E> {

        private final Map<Thread, byte[]> buffers = new ConcurrentHashMap<>();
        private int stopCount;

        @Override
        public byte[] encode(E event) {
            buffers.computeIfAbsent(Thread.currentThread(), t -> new byte[1024]);
            return String.valueOf(event).getBytes();
        }

        @Override
        public byte[] headerBytes() {
            return null;
        }

        @Override
        public byte[] footerBytes() {
            return null;
        }

        @Override
        public void stop() {
            stopCount++;
            buffers.clear();
            super.stop();
        }

        int retainedBufferCount() {
            return buffers.size();
        }

        int stopCount() {
            return stopCount;
        }
    }
}
