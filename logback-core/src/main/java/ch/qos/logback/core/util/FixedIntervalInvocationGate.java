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

import java.util.concurrent.atomic.AtomicLong;

/**
 * A time-based {@link InvocationGate} with fixed-interval logic.
 * <p>
 * Callers on a hot path use {@link #isTooSoon(long)} to decide whether to skip
 * a costly operation. At most one thread per {@linkplain #increment increment}
 * interval is allowed to proceed (i.e. receives {@code false}); other threads
 * in the same window receive {@code true} and should skip the work.
 * </p>
 * <p>
 * Compared to {@link DefaultInvocationGate}, this implementation does not adapt
 * a sampling mask. It only advances an atomic next-allowed timestamp by a fixed
 * {@link Duration}.
 * </p>
 * <p>
 * Typical use is size checks in rolling policies where file length is expensive
 * relative to the logging call.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.6/1.4.6 (formerly {@link SimpleInvocationGate})
 * @see InvocationGate
 * @see DefaultInvocationGate
 * @see BatchedFixedIntervalInvocationGate
 * @see ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
 */
public class FixedIntervalInvocationGate implements InvocationGate {

    /**
     * Next time (milliseconds since the epoch) at or after which a caller may be
     * allowed to proceed. Updated with compare-and-set so that only one thread
     * wins per interval.
     */
    AtomicLong atomicNext = new AtomicLong(0);

    /**
     * Minimum time between allowed invocations.
     */
    final Duration increment;

    /**
     * Default increment: 60 seconds.
     */
    final public static Duration DEFAULT_INCREMENT = Duration.buildBySeconds(60);

    /**
     * Creates a gate with {@link #DEFAULT_INCREMENT}.
     */
    public FixedIntervalInvocationGate() {
        this(DEFAULT_INCREMENT);
    }

    /**
     * Creates a gate that allows at most one successful passage per
     * {@code anIncrement} period.
     *
     * @param anIncrement duration between allowed invocations; must not be
     *                    {@code null}
     */
    public FixedIntervalInvocationGate(Duration anIncrement) {
        this.increment = anIncrement;
    }

    /**
     * Returns {@code true} if the caller should skip further work; {@code false}
     * if this call is allowed to proceed.
     * <p>
     * If {@code currentTime} is {@link InvocationGate#TIME_UNAVAILABLE}
     * ({@code -1}), this method returns {@code false} so the caller can still
     * perform the work when the clock is unavailable.
     * </p>
     * <p>
     * Otherwise, when {@code currentTime} is strictly before the next allowed
     * time, the method returns {@code true} (too soon). When
     * {@code currentTime} has reached the next allowed time, this thread tries
     * to advance that time by {@link #increment}. On success it returns
     * {@code false} (proceed); if another thread already advanced it, this
     * thread returns {@code true} so that only one passage per interval is
     * granted.
     * </p>
     *
     * @param currentTime current time in milliseconds, or
     *                    {@link InvocationGate#TIME_UNAVAILABLE} if unknown
     * @return {@code true} if further work should be skipped; {@code false} if
     *         the caller may proceed
     */
    @Override
    public boolean isTooSoon(long currentTime) {
        if (currentTime == -1)
            return false;

        long localNext = atomicNext.get();
        if (currentTime >= localNext) {
            long next2 = currentTime + increment.getMilliseconds();
            // if success, we were able to set the variable, otherwise some other thread beat us to it
            boolean success = atomicNext.compareAndSet(localNext, next2);
            // while we have crossed 'next', the other thread already returned true. There is
            // no point in letting more than one thread per duration.
            return !success;
        } else {
            return true;
        }

    }
}
