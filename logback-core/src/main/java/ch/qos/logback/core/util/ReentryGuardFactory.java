/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2025, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.util;

/**
 * Factory that creates {@link ReentryGuard} instances according to a requested type.
 *
 * <p>This class centralizes creation of the built-in guard implementations.
 * Consumers can use the factory to obtain either a per-thread guard or a no-op
 * guard depending on their needs.</p>
 *
 * @since 1.5.21
 */
public class ReentryGuardFactory {

    /**
     * Types of guards that can be produced by this factory.
     *
     * THREAD_LOCAL - returns a {@link ReentryGuard.ReentryGuardImpl} backed by a ThreadLocal.
     * NOP - returns a {@link ReentryGuard.NOPRentryGuard} which never locks.
     */
    public enum GuardType {
        THREAD_LOCAL,
        NOP
    }


    /**
     * Create a {@link ReentryGuard} for the given {@link GuardType}.
     *
     * <p>Returns a fresh instance of the requested guard implementation. The
     * factory does not cache instances; callers may obtain separate instances
     * as required.</p>
     *
     * <p>Thread-safety: this method is stateless and may be called concurrently
     * from multiple threads.</p>
     *
     * @param guardType the type of guard to create; must not be {@code null}
     * @return a new {@link ReentryGuard} instance implementing the requested semantics
     * @throws NullPointerException if {@code guardType} is {@code null}
     * @throws IllegalArgumentException if an unknown guard type is provided
     * @since 1.5.21
     */
    public static ReentryGuard makeGuard(GuardType guardType) {
        switch (guardType) {
            case THREAD_LOCAL:
                return new ReentryGuard.ReentryGuardImpl();
            case NOP:
                return new ReentryGuard.NOPRentryGuard();
            default:
                throw new IllegalArgumentException("Unknown GuardType: " + guardType);
        }
    }
}
