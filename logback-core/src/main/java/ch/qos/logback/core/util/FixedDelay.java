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
    
    
    @Override
    public String toString() {
        return "FixedDelay [subsequentDelay=" + subsequentDelay + ", nextDelay=" + nextDelay + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (nextDelay ^ (nextDelay >>> 32));
        result = prime * result + (int) (subsequentDelay ^ (subsequentDelay >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FixedDelay other = (FixedDelay) obj;
        if (nextDelay != other.nextDelay)
            return false;
        if (subsequentDelay != other.subsequentDelay)
            return false;
        return true;
    }

}
