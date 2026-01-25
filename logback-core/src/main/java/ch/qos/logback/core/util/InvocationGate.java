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

public interface InvocationGate {

    final long TIME_UNAVAILABLE = -1;

    /**
     * The caller of this method can decide to skip further work if the returned
     * value is true.
     * 
     * Implementations should be able to give a reasonable answer even if current
     * time date is unavailable.
     * 
     * @param currentTime can be TIME_UNAVAILABLE (-1) to signal that time is not
     *                    available
     * @return if true, caller should skip further work
     */
    public abstract boolean isTooSoon(long currentTime);

}