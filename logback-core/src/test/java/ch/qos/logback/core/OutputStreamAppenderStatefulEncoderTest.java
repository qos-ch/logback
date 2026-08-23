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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.encoder.StatefulTestEncoder;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;

/**
 * Tests {@link OutputStreamAppender} (and prudent-mode {@link FileAppender})
 * when {@link ch.qos.logback.core.encoder.Encoder#isStateful() Encoder.isStateful()}
 * is {@code true}.
 *
 * <p>A stateful encoder must not see overlapping {@code headerBytes}/{@code encode}/
 * {@code footerBytes} calls, {@code encode} before the header, or {@code encode} after
 * the footer. {@link StatefulTestEncoder} records those violations and writes a
 * bracketed list such as {@code [a,b,c]}.</p>
 */
public class OutputStreamAppenderStatefulEncoderTest {

    private static final int THREAD_COUNT = 8;
    private static final int LOOP_COUNT = 50;
    private static final long JOIN_TIMEOUT_MILLIS = 10_000;

    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);
    int diff = RandomUtil.getPositiveInt();

    @BeforeEach
    public void setUp() {
        context.setName("stateful-encoder-test-" + diff);
    }

    /**
     * Sequential happy path: {@code start()} writes the header, each append
     * is encoded in order, and {@code stop()} writes the footer.
     */
    @Test
    public void startWritesHeaderBeforeEventsAndStopWritesFooter() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StatefulTestEncoder<Object> encoder = newEncoder();
        OutputStreamAppender<Object> osa = newAppender(encoder, baos);

        osa.start();
        osa.doAppend("a");
        osa.doAppend("b");
        osa.stop();

        assertEncoderContract(encoder, 2);
        assertEquals("[a,b]", baos.toString(StandardCharsets.UTF_8));
        statusChecker.assertIsWarningOrErrorFree();
    }

    /**
     * Concurrent {@code doAppend} must serialize {@code encode()} so the
     * encoder never observes overlapping calls and the byte stream stays a
     * well-formed list.
     */
    @Test
    public void concurrentAppendDoesNotOverlapEncode() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StatefulTestEncoder<Object> encoder = newEncoder();
        OutputStreamAppender<Object> osa = newAppender(encoder, baos);
        osa.start();

        int expected = THREAD_COUNT * LOOP_COUNT;
        runConcurrentAppends(osa);
        osa.stop();

        assertEncoderContract(encoder, expected);
        assertWellFormedList(baos.toString(StandardCharsets.UTF_8), expected);
        statusChecker.assertIsWarningOrErrorFree();
    }

    /**
     * Appends racing {@code start()} must wait until {@code headerBytes()} has
     * finished. {@link StatefulTestEncoder#setDelayLifecycle(boolean)} widens
     * the window between {@code started=true} and the header write.
     */
    @Test
    public void concurrentAppendDuringStartDoesNotEncodeBeforeHeader() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StatefulTestEncoder<Object> encoder = newEncoder();
        encoder.setDelayLifecycle(true);
        OutputStreamAppender<Object> osa = newAppender(encoder, baos);

        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch go = new CountDownLatch(1);
        List<Thread> threads = startThreads(ready, go, () -> {
            while (!osa.isStarted()) {
                Thread.yield();
            }
            osa.doAppend("x");
        });

        awaitLatch(ready);
        go.countDown();
        osa.start();
        joinAll(threads);
        osa.stop();

        assertFalse(encoder.hadEncodeBeforeHeader(), "encode() ran before headerBytes()");
        assertFalse(encoder.hadOverlap());
        assertFalse(encoder.hadEncodeAfterFooter());
        assertEquals(THREAD_COUNT, encoder.getEncodeCount());
        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue(output.startsWith(StatefulTestEncoder.HEADER), output);
        assertTrue(output.endsWith(StatefulTestEncoder.FOOTER), output);
        assertWellFormedList(output, encoder.getEncodeCount());
        statusChecker.assertIsWarningOrErrorFree();
    }

    /**
     * Appends racing {@code stop()} must not call {@code encode()} after
     * {@code footerBytes()}. The delayed footer write makes the stop/append
     * interleaving easier to hit.
     */
    @Test
    public void concurrentAppendDuringStopDoesNotEncodeAfterFooter() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StatefulTestEncoder<Object> encoder = newEncoder();
        encoder.setDelayLifecycle(true);
        OutputStreamAppender<Object> osa = newAppender(encoder, baos);
        osa.start();

        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch go = new CountDownLatch(1);
        List<Thread> threads = startThreads(ready, go, () -> {
            while (osa.isStarted()) {
                osa.doAppend("x");
                Thread.yield();
            }
        });

        awaitLatch(ready);
        go.countDown();
        Thread.sleep(20);
        osa.stop();
        joinAll(threads);

        assertFalse(encoder.hadEncodeAfterFooter(), "encode() ran after footerBytes()");
        assertFalse(encoder.hadEncodeBeforeHeader());
        assertFalse(encoder.hadOverlap());
        assertTrue(encoder.getEncodeCount() > 0);
        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue(output.startsWith(StatefulTestEncoder.HEADER), output);
        assertTrue(output.endsWith(StatefulTestEncoder.FOOTER), output);
        assertWellFormedList(output, encoder.getEncodeCount());
        statusChecker.assertIsWarningOrErrorFree();
    }

    /**
     * Prudent-mode {@link FileAppender} still serializes stateful {@code encode()}
     * under {@code streamWriteLock} (in addition to {@code FileChannel.lock()}
     * for cross-JVM exclusion). Concurrent appends must not overlap or produce
     * {@code OverlappingFileLockException}.
     */
    @Test
    public void prudentModeConcurrentAppend() throws Exception {
        String outputDirStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "statefulPrudent-" + diff + "/";
        String logfileStr = outputDirStr + "output.log";
        File outputDir = new File(outputDirStr);
        if (!outputDir.mkdirs() && !outputDir.isDirectory()) {
            fail("failed to create folder " + outputDir);
        }

        StatefulTestEncoder<Object> encoder = newEncoder();
        FileAppender<Object> fa = new FileAppender<Object>();
        fa.setContext(context);
        fa.setName("FILE");
        fa.setPrudent(true);
        fa.setEncoder(encoder);
        fa.setFile(logfileStr);
        fa.start();

        int expected = THREAD_COUNT * LOOP_COUNT;
        runConcurrentAppends(fa);
        fa.stop();

        assertEncoderContract(encoder, expected);
        String output = Files.readString(new File(logfileStr).toPath(), StandardCharsets.UTF_8);
        assertWellFormedList(output, expected);
        statusChecker.assertIsWarningOrErrorFree();
    }

    private StatefulTestEncoder<Object> newEncoder() {
        StatefulTestEncoder<Object> encoder = new StatefulTestEncoder<Object>();
        encoder.setContext(context);
        return encoder;
    }

    private OutputStreamAppender<Object> newAppender(StatefulTestEncoder<Object> encoder, ByteArrayOutputStream baos) {
        OutputStreamAppender<Object> osa = new OutputStreamAppender<Object>();
        osa.setContext(context);
        osa.setName("OSA");
        osa.setEncoder(encoder);
        osa.setOutputStream(baos);
        return osa;
    }

    private void runConcurrentAppends(Appender<Object> appender) throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch go = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>(THREAD_COUNT);
        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadIndex = t;
            Thread thread = new Thread(() -> {
                ready.countDown();
                try {
                    go.await();
                    for (int i = 0; i < LOOP_COUNT; i++) {
                        if ((i & 0x08) == 0) {
                            Thread.yield();
                        }
                        appender.doAppend(threadIndex + "-" + i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            thread.start();
            threads.add(thread);
        }
        awaitLatch(ready);
        go.countDown();
        joinAll(threads);
    }

    private List<Thread> startThreads(CountDownLatch ready, CountDownLatch go, Runnable body) {
        List<Thread> threads = new ArrayList<>(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(() -> {
                ready.countDown();
                try {
                    go.await();
                    body.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            thread.start();
            threads.add(thread);
        }
        return threads;
    }

    private void awaitLatch(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(JOIN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "threads did not become ready");
    }

    private void joinAll(List<Thread> threads) throws InterruptedException {
        for (Thread thread : threads) {
            thread.join(JOIN_TIMEOUT_MILLIS);
            assertFalse(thread.isAlive(), "thread did not finish: " + thread.getName());
        }
    }

    private void assertEncoderContract(StatefulTestEncoder<Object> encoder, int expectedEncodeCount) {
        assertFalse(encoder.hadOverlap(), "header/encode/footer overlapped");
        assertFalse(encoder.hadEncodeBeforeHeader(), "encode() ran before headerBytes()");
        assertFalse(encoder.hadEncodeAfterFooter(), "encode() ran after footerBytes()");
        assertEquals(expectedEncodeCount, encoder.getEncodeCount());
    }

    private void assertWellFormedList(String output, int expectedCount) {
        assertTrue(output.startsWith(StatefulTestEncoder.HEADER), output);
        assertTrue(output.endsWith(StatefulTestEncoder.FOOTER), output);
        String inner = output.substring(StatefulTestEncoder.HEADER.length(),
                output.length() - StatefulTestEncoder.FOOTER.length());
        if (expectedCount == 0) {
            assertEquals("", inner);
            return;
        }
        String[] tokens = inner.split(",", -1);
        assertEquals(expectedCount, tokens.length, output);
        for (String token : tokens) {
            assertFalse(token.isEmpty(), output);
        }
    }
}
