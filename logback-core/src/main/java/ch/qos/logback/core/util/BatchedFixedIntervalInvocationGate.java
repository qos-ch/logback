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
package ch.qos.logback.core.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An {@link InvocationGate} that allows a fixed number of successive callers to
 * proceed, then enforces a lull of a fixed {@link Duration}.
 * <p>
 * Callers on a hot path use {@link #isTooSoon(long)} to decide whether to skip a
 * costly operation. Up to {@linkplain #batchSize N} invocations may receive
 * {@code false} (proceed) in succession. After the N-th success, the gate enters
 * a lull of {@linkplain #increment increment} milliseconds during which all
 * callers receive {@code true} (skip). When the lull ends, another batch of N
 * is available.
 * </p>
 * <p>
 * This extends the idea of {@link FixedIntervalInvocationGate} (at most one
 * success per interval) to batches of size N. Token accounting and the lull
 * deadline use atomics so concurrent callers share one batch.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.6.3
 * @see InvocationGate
 * @see FixedIntervalInvocationGate
 */
public class BatchedFixedIntervalInvocationGate implements InvocationGate {

    /**
     * Default batch size: number of successive allowed passages before a lull.
     */
    public static final int DEFAULT_BATCH_SIZE = 8;

    /**
     * Default lull duration after a batch is exhausted (same as
     * {@link FixedIntervalInvocationGate#DEFAULT_INCREMENT}).
     */
    public static final Duration DEFAULT_INCREMENT = FixedIntervalInvocationGate.DEFAULT_INCREMENT;

    /**
     * Number of successive allowed invocations per open batch.
     */
    final int batchSize;

    /**
     * Duration of the lull after a batch is exhausted.
     */
    final Duration increment;

    /**
     * Remaining tokens in the current batch. Re-armed to {@link #batchSize}
     * when the last token of a batch is taken (the lull deadline is set first).
     */
    final AtomicInteger remaining;

    /**
     * Earliest time (milliseconds since the epoch) at which a new batch may be
     * used. While {@code currentTime} is strictly less than this value, the
     * gate is in lull and all callers are too soon.
     */
    final AtomicLong atomicNext = new AtomicLong(0);

    /**
     * Creates a gate with {@link #DEFAULT_BATCH_SIZE} and
     * {@link #DEFAULT_INCREMENT}.
     */
    public BatchedFixedIntervalInvocationGate() {
        this(DEFAULT_BATCH_SIZE, DEFAULT_INCREMENT);
    }

    /**
     * Creates a gate with the given batch size and {@link #DEFAULT_INCREMENT}.
     *
     * @param batchSize number of successive allowed invocations per batch; must
     *                  be at least 1
     */
    public BatchedFixedIntervalInvocationGate(int batchSize) {
        this(batchSize, DEFAULT_INCREMENT);
    }

    /**
     * Creates a gate that allows {@code batchSize} successive passages, then
     * lulls for {@code anIncrement}.
     *
     * @param batchSize   number of successive allowed invocations per batch;
     *                    must be at least 1
     * @param anIncrement lull duration after a batch is exhausted; must not be
     *                    {@code null}
     */
    public BatchedFixedIntervalInvocationGate(int batchSize, Duration anIncrement) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be at least 1, was " + batchSize);
        }
        if (anIncrement == null) {
            throw new IllegalArgumentException("increment must not be null");
        }
        this.batchSize = batchSize;
        this.increment = anIncrement;
        this.remaining = new AtomicInteger(batchSize);
    }

    /**
     * Returns {@code true} if the caller should skip further work; {@code false}
     * if this call is allowed to proceed.
     * <p>
     * If {@code currentTime} is {@link InvocationGate#TIME_UNAVAILABLE}
     * ({@code -1}), returns {@code false} so work can still run when the clock
     * is unavailable.
     * </p>
     * <p>
     * During a lull ({@code currentTime} before the next open time), returns
     * {@code true}. Otherwise tries to consume one batch token. The first
     * {@code batchSize} successful consumptions return {@code false}. The
     * caller that takes the last token starts a lull of {@link #increment} and
     * re-arms the batch for after the lull.
     * </p>
     *
     * @param currentTime current time in milliseconds, or
     *                    {@link InvocationGate#TIME_UNAVAILABLE} if unknown
     * @return {@code true} if further work should be skipped; {@code false} if
     *         the caller may proceed
     */
    @Override
    public boolean isTooSoon(long currentTime) {
        if (currentTime == -1) {
            return false;
        }

        if (currentTime < atomicNext.get()) {
            return true;
        }

        while (true) {
            int left = remaining.get();
            if (left <= 0) {
                // Last token already taken; lull is being established or in force.
                return true;
            }
            if (remaining.compareAndSet(left, left - 1)) {
                if (left == 1) {
                    // Order matters: set lull deadline before re-arming tokens so
                    // concurrent callers see the lull and do not drain the new batch.
                    atomicNext.set(currentTime + increment.getMilliseconds());
                    remaining.set(batchSize);
                }
                return false;
            }
        }
    }
}
