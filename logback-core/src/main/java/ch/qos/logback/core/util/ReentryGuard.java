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

/**
 * Guard used to prevent re-entrant (recursive) appender invocations on a per-thread basis.
 *
 * <p>Implementations are used by appenders and other components that must avoid
 * recursively calling back into themselves (for example when an error causes
 * logging while handling a logging event). Typical usage: check {@link #isLocked()}
 * before proceeding and call {@link #lock()} / {@link #unlock()} around the
 * guarded region.</p>
 *
 * <p>Concurrency: guards operate on a per-thread basis; callers should treat the
 * guard as thread-local state. Implementations must document their semantics;
 * the provided {@link ReentryGuardImpl} uses a {@link ThreadLocal} to track the
 * locked state for the current thread.</p>
*
 * @since 1.5.21
 */
public interface ReentryGuard {

    /**
     * Return true if the current thread holds the guard (i.e. is inside a guarded region).
     *
     * <p>Implementations typically return {@code false} if the current thread has not
     * previously called {@link #lock()} or if the stored value is {@code null}.</p>
     *
     * @return {@code true} if the guard is locked for the current thread, {@code false} otherwise
     */
    boolean isLocked();

    /**
     * Mark the guard as locked for the current thread.
     *
     * <p>Callers must ensure {@link #unlock()} is invoked in a finally block to
     * avoid leaving the guard permanently locked for the thread.</p>
     */
    void lock();

    /**
     * Release the guard for the current thread.
     *
     * <p>After calling {@code unlock()} the {@link #isLocked()} should return
     * {@code false} for the current thread (unless {@code lock()} is called again).</p>
     */
    void unlock();


    /**
     * Default per-thread implementation backed by a {@link ThreadLocal<Boolean>}.
     *
     * <p>Semantics: a value of {@link Boolean#TRUE} indicates the current thread
     * is inside a guarded region. If the ThreadLocal has no value ({@code null}),
     * {@link #isLocked()} treats this as unlocked (returns {@code false}).</p>
     *
     * <p>Note: this implementation intentionally uses {@code ThreadLocal<Boolean>}
     * to avoid global synchronization. The initial state is unlocked.</p>
     *
     * Typical usage:
     * <pre>
     * if (!guard.isLocked()) {
     *   guard.lock();
     *   try {
     *     // guarded work
     *   } finally {
     *     guard.unlock();
     *   }
     * }
     * </pre>
     *
     */
    class ReentryGuardImpl implements ReentryGuard {

        private ThreadLocal<Boolean> guard = new ThreadLocal<Boolean>();


        @Override
        public boolean isLocked() {
            // the guard is considered locked if the ThreadLocal contains Boolean.TRUE
            // note that initially the ThreadLocal contains null
            return (Boolean.TRUE.equals(guard.get()));
        }

        @Override
        public void lock() {
            guard.set(Boolean.TRUE);
        }

        @Override
        public void unlock() {
            guard.set(Boolean.FALSE);
        }
    }

    /**
     * No-op implementation that never locks. Useful in contexts where re-entrancy
     * protection is not required.
     *
     * <p>{@link #isLocked()} always returns {@code false}. {@link #lock()} and
     * {@link #unlock()} are no-ops.</p>
     *
     * <p>Use this implementation when the caller explicitly wants to disable
     * reentrancy protection (for example in tests or in environments where the
     * cost of thread-local checks is undesirable and re-entrancy cannot occur).</p>
     *
     */
    class NOPRentryGuard implements ReentryGuard {
        @Override
        public boolean isLocked() {
            return false;
        }

        @Override
        public void lock() {
            // NOP
        }

        @Override
        public void unlock() {
            // NOP
        }
    }

}
