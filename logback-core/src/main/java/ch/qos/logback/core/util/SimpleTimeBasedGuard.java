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
 * A simple time-based guard that limits the number of allowed operations within a sliding time window.
 * This class is useful for rate limiting or preventing excessive actions over time periods.
 * It supports time injection for testing purposes.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.5.22
 */
public class SimpleTimeBasedGuard {

    private final long windowDurationMs;
    private final int maxAllows;

    /**
     * Default window duration in milliseconds: 30 minutes.
     */
    public static final long DEFAULT_WINDOW_MS = 30*60_000L; // 30 minutes

    /**
     * Default maximum number of allows per window: 2.
     */
    public static final int DEFAULT_MAX_ALLOWS = 2;

    // Injectable time
    private final AtomicLong artificialTime = new AtomicLong(-1L);

    // Current window state
    private volatile long windowStartMs = 0;
    private volatile int allowsUsed = 0;

    /**
     * Creates a guard with custom limits.
     *
     * @param windowDurationMs how many millis per window (e.g. 30_000 for 30 minutes)
     * @param maxAllows        how many allows per window (e.g. 2)
     */
    public SimpleTimeBasedGuard(long windowDurationMs, int maxAllows) {
        if (windowDurationMs <= 0) throw new IllegalArgumentException("windowDurationMs must be > 0");
        if (maxAllows < 1) throw new IllegalArgumentException("maxAllows must be >= 1");

        this.windowDurationMs = windowDurationMs;
        this.maxAllows = maxAllows;
    }

    /**
     * Convenience: uses defaults — 2 allows every 30 minutes
     */
    public SimpleTimeBasedGuard() {
        this(DEFAULT_WINDOW_MS, DEFAULT_MAX_ALLOWS);
    }

    /**
     * Checks if an operation is allowed based on the current time window.
     * If allowed, increments the usage count for the current window.
     * If the window has expired, resets the window and allows the operation.
     *
     * @return true if the operation is allowed, false otherwise
     */
    public synchronized boolean allow() {
        long now = currentTimeMillis();

        // First call ever
        if (windowStartMs == 0) {
            windowStartMs = now;
            allowsUsed = 1;
            return true;
        }

        // Still in current window?
        if (now < windowStartMs + windowDurationMs) {
            if (allowsUsed < maxAllows) {
                allowsUsed++;
                return true;
            }
            return false;
        }

        // New window → reset
        windowStartMs = now;
        allowsUsed = 1;
        return true;
    }

    // --- Time injection for testing ---

    /**
     * Sets the artificial current time for testing purposes.
     * When set, {@link #currentTimeMillis()} will return this value instead of {@link System#currentTimeMillis()}.
     *
     * @param timestamp the artificial timestamp in milliseconds
     */
    public void setCurrentTimeMillis(long timestamp) {
        this.artificialTime.set(timestamp);
    }

    /**
     * Clears the artificial time, reverting to using {@link System#currentTimeMillis()}.
     */
    public void clearCurrentTime() {
        this.artificialTime.set(-1L);
    }

    private long currentTimeMillis() {
        long t = artificialTime.get();
        return t >= 0 ? t : System.currentTimeMillis();
    }

    void incCurrentTimeMillis(long increment) {
        artificialTime.getAndAdd(increment);
    }

    // --- Helpful getters ---

    /**
     * Returns the number of allows used in the current window.
     *
     * @return the number of allows used
     */
    public int getAllowsUsed() {
        return allowsUsed;
    }

    /**
     * Returns the number of allows remaining in the current window.
     *
     * @return the number of allows remaining
     */
    public int getAllowsRemaining() {
        return Math.max(0, maxAllows - allowsUsed);
    }

    /**
     * Returns the window duration in milliseconds.
     *
     * @return the window duration in milliseconds
     */
    public long getWindowDuration() {
        return windowDurationMs;
    }

    /**
     * Returns the maximum number of allows per window.
     *
     * @return the maximum number of allows
     */
    public int getMaxAllows() {
        return maxAllows;
    }

    /**
     * Returns the number of milliseconds until the next window starts.
     * If no window has started yet, returns the full window duration.
     *
     * @return milliseconds until next window
     */
    public long getMillisUntilNextWindow() {
        if (windowStartMs == 0) return windowDurationMs;
        long nextWindowStart = windowStartMs + windowDurationMs;
        long now = currentTimeMillis();
        return Math.max(0, nextWindowStart - now);
    }
}
