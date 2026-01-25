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

package ch.qos.logback.core.rolling;

class ConfigParameters {

    long simulatedTime;
    int maxHistory;
    int simulatedNumberOfPeriods;
    int startInactivity = -1;
    int numInactivityPeriods;
    String fileNamePattern;
    long periodDurationInMillis = TimeBasedRollingWithArchiveRemoval_Test.MILLIS_IN_DAY;
    long sizeCap;

    ConfigParameters(long simulatedTime) {
        this.simulatedTime = simulatedTime;
    }

    ConfigParameters maxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
        return this;
    }

    ConfigParameters simulatedNumberOfPeriods(int simulatedNumberOfPeriods) {
        this.simulatedNumberOfPeriods = simulatedNumberOfPeriods;
        return this;
    }

    ConfigParameters startInactivity(int startInactivity) {
        this.startInactivity = startInactivity;
        return this;
    }

    ConfigParameters numInactivityPeriods(int numInactivityPeriods) {
        this.numInactivityPeriods = numInactivityPeriods;
        return this;
    }

    ConfigParameters fileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
        return this;
    }

    ConfigParameters periodDurationInMillis(long periodDurationInMillis) {
        this.periodDurationInMillis = periodDurationInMillis;
        return this;
    }

    ConfigParameters sizeCap(long sizeCap) {
        this.sizeCap = sizeCap;
        return this;
    }
}