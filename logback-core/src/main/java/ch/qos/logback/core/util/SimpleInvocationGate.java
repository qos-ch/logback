/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.util;

public class SimpleInvocationGate implements InvocationGate {

    volatile long next = 0;
    final long increment;
    final public static int DEFAULT_INCREMENT = 10_000;

    public SimpleInvocationGate() {
        this(DEFAULT_INCREMENT);
    }

    public SimpleInvocationGate(int anIncrement) {
        this.increment = anIncrement;
    }

    @Override
    public boolean isTooSoon(long currentTime) {
        if(currentTime == -1)
            return false;

        if(currentTime >= next) {
            next = currentTime + increment;
            return false;
        }
        return true;
    }
}
