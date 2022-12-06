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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * An invocation gate using very simple logic.
 *
 * @since 1.3.6/1.4.6
 */
public class SimpleInvocationGate implements InvocationGate {

    //volatile long next = 0;

    AtomicLong atomicNext = new AtomicLong(0);
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
        if (currentTime == -1)
            return false;

        long localNext = atomicNext.get();
        if (currentTime >= localNext) {
            long next2 = currentTime+increment;
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

//    private final boolean isTooSoonSynchronized(long currentTime) {
//        if (currentTime == -1)
//            return false;
//
//        synchronized (this) {
//            if (currentTime >= next) {
//                next = currentTime + increment;
//                return false;
//            }
//        }
//        return true;
//    }

