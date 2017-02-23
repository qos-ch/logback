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
package ch.qos.logback.access.filter;

abstract public class PeriodicStats {

    private long nextPeriodBegins = 0;
    private long lastTotal = 0;
    private long lastCount = 0;

    private double average;
    private int n;

    PeriodicStats() {
        this(System.currentTimeMillis());
    }

    PeriodicStats(long now) {
        nextPeriodBegins = computeStartOfNextPeriod(now);
    }

    void update(long now, long total) {
        if (now > nextPeriodBegins) {
            lastCount = total - lastTotal;
            lastTotal = total;
            average = (average * n + lastCount) / (++n);
            nextPeriodBegins = computeStartOfNextPeriod(now);
        }
    }

    public double getAverage() {
        return average;
    }

    public long getLastCount() {
        return lastCount;
    }

    void reset(long now) {
        nextPeriodBegins = computeStartOfNextPeriod(now);
        lastTotal = 0;
        lastCount = 0;
        average = 0.0;
        n = 0;
    }

    void reset() {
        reset(System.currentTimeMillis());
    }

    abstract long computeStartOfNextPeriod(long now);

}
