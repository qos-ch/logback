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
 * This class serves as a gateway for invocations of a "costly" operation on a critical execution path.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class DefaultInvocationGate implements InvocationGate {

    static final int MASK_DECREASE_RIGHT_SHIFT_COUNT = 2;

    // experiments indicate that even for the most CPU intensive applications with 200 or more threads MASK
    // values in the order of 0xFFFF is appropriate
    private static final int MAX_MASK = 0xFFFF;
    static final int DEFAULT_MASK = 0xF;

    private volatile long mask = DEFAULT_MASK;
    //private volatile long lastMaskCheck = System.currentTimeMillis();

    // IMPORTANT: This field can be updated by multiple threads. It follows that
    // its values may *not* be incremented sequentially. However, we don't care
    // about the actual value of the field except that from time to time the
    // expression (invocationCounter++ & mask) == mask) should be true.
    private long invocationCounter = 0;

    // if less than thresholdForMaskIncrease milliseconds elapse between invocations of updateMaskIfNecessary()
    // method, then the mask should be increased
    private static final long MASK_INCREASE_THRESHOLD = 100;

    // if more than thresholdForMaskDecrease milliseconds elapse between invocations of updateMaskIfNecessary() method,
    // then the mask should be decreased
    private static final long MASK_DECREASE_THRESHOLD = MASK_INCREASE_THRESHOLD * 8;

    
    public DefaultInvocationGate() {
        this(MASK_INCREASE_THRESHOLD, MASK_DECREASE_THRESHOLD, System.currentTimeMillis());
    }
    
    public  DefaultInvocationGate(long minDelayThreshold, long maxDelayThreshold, long currentTime) {
        this.minDelayThreshold = minDelayThreshold;
        this.maxDelayThreshold = maxDelayThreshold; 
        this.lowerLimitForMaskMatch = currentTime + minDelayThreshold;
        this.upperLimitForNoMaskMatch = currentTime + maxDelayThreshold;
    }
    
    private long minDelayThreshold;
    private long maxDelayThreshold;

    long lowerLimitForMaskMatch;
    long upperLimitForNoMaskMatch;

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.util.InvocationGate#skipFurtherWork()
     */
    @Override
    final public boolean isTooSoon(long currentTime) {
        boolean maskMatch = ((invocationCounter++) & mask) == mask;

        if (maskMatch) {
            if (currentTime < this.lowerLimitForMaskMatch) {
                increaseMask();
            }
            updateLimits(currentTime);
        } else {
            if (currentTime > this.upperLimitForNoMaskMatch) {
                decreaseMask();
                updateLimits(currentTime);
                return false;
            }
        }
        return !maskMatch;
    }

    private void updateLimits(long currentTime) {
        this.lowerLimitForMaskMatch = currentTime + minDelayThreshold;
        this.upperLimitForNoMaskMatch = currentTime + maxDelayThreshold;
    }


    // package private, for testing purposes only
    long getMask() {
        return mask;
    }
    
    private void increaseMask() {
        if (mask >= MAX_MASK)
            return;
        mask = (mask << 1) | 1;
    }

    private void decreaseMask() {
        mask = mask >>> MASK_DECREASE_RIGHT_SHIFT_COUNT;
    }

    public long getInvocationCounter() {
        return invocationCounter;
    }
}
