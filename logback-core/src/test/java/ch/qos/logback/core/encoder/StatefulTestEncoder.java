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
package ch.qos.logback.core.encoder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test encoder with {@link #isStateful()} {@code true}, used to assert that an
 * appender serializes {@link #headerBytes()}, {@link #encode(Object)}, and
 * {@link #footerBytes()} and keeps them in that order.
 *
 * <p>Lifecycle is a three-state machine:</p>
 * <ul>
 *   <li>{@link Phase#INIT} — before the header has been written. {@code encode}
 *       in this phase sets {@link #hadEncodeBeforeHeader()}.</li>
 *   <li>{@link Phase#OPEN} — set at the <em>end</em> of {@code headerBytes()}, so
 *       an {@code encode} that sneaks in during the header still counts as
 *       before-header.</li>
 *   <li>{@link Phase#CLOSED} — set at the <em>start</em> of {@code footerBytes()},
 *       so an {@code encode} during the footer counts as after-footer
 *       ({@link #hadEncodeAfterFooter()}).</li>
 * </ul>
 *
 * <p>{@link #enter()} / {@link #leave()} wrap every public encoder method. If a
 * second call arrives while one is in flight, {@link #hadOverlap()} becomes
 * true. That is the signal that {@code streamWriteLock} failed to serialize
 * the stateful path.</p>
 *
 * <p>Output is a bracketed comma-separated list assembled by concatenating
 * writes: {@code "["} + first event + {@code ","} + later events + {@code "]"}.
 * Concurrent {@code encode} without a lock can omit commas or interleave
 * tokens, which the tests detect as a malformed list.</p>
 *
 * @param <E> event type
 */
public class StatefulTestEncoder<E> extends EncoderBase<E> {

    public static final String HEADER = "[";
    public static final String FOOTER = "]";

    /**
     * Encoder lifecycle as observed by {@link #encode(Object)}.
     */
    enum Phase {
        INIT, OPEN, CLOSED
    }

    /** Current lifecycle; written in header/footer, read in encode. */
    private volatile Phase phase = Phase.INIT;

    /**
     * When true, {@link #headerBytes()} and {@link #footerBytes()} sleep so
     * concurrent appends have a wider window to race start/stop.
     */
    private volatile boolean delayLifecycle;

    /** Count of nested/overlapping header, encode, or footer calls. */
    private final AtomicInteger inFlight = new AtomicInteger();
    private final AtomicInteger encodeCount = new AtomicInteger();
    private final AtomicBoolean overlap = new AtomicBoolean();
    private final AtomicBoolean encodeBeforeHeader = new AtomicBoolean();
    private final AtomicBoolean encodeAfterFooter = new AtomicBoolean();

    /**
     * Always {@code true} so {@code OutputStreamAppender} takes the stateful
     * lock protocol.
     */
    @Override
    public boolean isStateful() {
        return true;
    }

    /**
     * Enable a short sleep in header/footer to enlarge start/stop races.
     */
    public void setDelayLifecycle(boolean delayLifecycle) {
        this.delayLifecycle = delayLifecycle;
    }

    /** {@code true} if two encoder methods ran at the same time. */
    public boolean hadOverlap() {
        return overlap.get();
    }

    /** {@code true} if {@code encode} ran before {@code headerBytes} completed. */
    public boolean hadEncodeBeforeHeader() {
        return encodeBeforeHeader.get();
    }

    /** {@code true} if {@code encode} ran after {@code footerBytes} began. */
    public boolean hadEncodeAfterFooter() {
        return encodeAfterFooter.get();
    }

    /** Number of {@code encode} invocations, including those that violated order. */
    public int getEncodeCount() {
        return encodeCount.get();
    }

    /**
     * Writes {@link #HEADER} and only then marks the encoder {@link Phase#OPEN}.
     */
    @Override
    public byte[] headerBytes() {
        enter();
        try {
            maybeDelay();
            phase = Phase.OPEN;
            return HEADER.getBytes(StandardCharsets.UTF_8);
        } finally {
            leave();
        }
    }

    /**
     * Records order/overlap violations, then emits the event with a leading
     * comma except for the first event. {@link Thread#yield()} widens the
     * overlap window if the caller is not holding {@code streamWriteLock}.
     */
    @Override
    public byte[] encode(E event) {
        enter();
        try {
            Phase current = phase;
            if (current == Phase.INIT) {
                encodeBeforeHeader.set(true);
            } else if (current == Phase.CLOSED) {
                encodeAfterFooter.set(true);
            }
            Thread.yield();
            int n = encodeCount.getAndIncrement();
            String token = (n == 0 ? "" : ",") + event;
            return token.getBytes(StandardCharsets.UTF_8);
        } finally {
            leave();
        }
    }

    /**
     * Marks {@link Phase#CLOSED} first so a concurrent {@code encode} is
     * reported as after-footer, then writes {@link #FOOTER}.
     */
    @Override
    public byte[] footerBytes() {
        enter();
        try {
            phase = Phase.CLOSED;
            maybeDelay();
            return FOOTER.getBytes(StandardCharsets.UTF_8);
        } finally {
            leave();
        }
    }

    /**
     * Detect overlapping encoder calls: {@code getAndIncrement() != 0} means
     * another header/encode/footer is already running.
     */
    private void enter() {
        if (inFlight.getAndIncrement() != 0) {
            overlap.set(true);
        }
    }

    private void leave() {
        inFlight.decrementAndGet();
    }

    private void maybeDelay() {
        if (!delayLifecycle) {
            return;
        }
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
