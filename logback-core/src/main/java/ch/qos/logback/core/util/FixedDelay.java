/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

/**
 * A default {@link DelayStrategy} that implements a simple fixed delay.
 *
 * @author Carl Harris
 * @since 1.1.0
 */
public class FixedDelay implements DelayStrategy {

    private final long subsequentDelay;
    private long nextDelay;

    /**
     * Initialize a new {@code FixedDelay} with a given {@code initialDelay} and
     * {@code subsequentDelay}.
     *
     * @param initialDelay    value for the initial delay
     * @param subsequentDelay value for all other delays
     */
    public FixedDelay(long initialDelay, long subsequentDelay) {
        this.nextDelay = initialDelay;
        this.subsequentDelay = subsequentDelay;
    }

    /**
     * Initialize a new {@code FixedDelay} with fixed delay value given by {@code delay}
     * parameter.
     *
     * @param delay value for all delays
     */
    public FixedDelay(int delay) {
        this(delay, delay);
    }

    /**
     * {@inheritDoc}
     */
    public long nextDelay() {
        long delay = nextDelay;
        nextDelay = subsequentDelay;
        return delay;
    }

}
