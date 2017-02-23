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
package ch.qos.logback.core.rolling.helper;

public enum PeriodicityType {

    ERRONEOUS, TOP_OF_MILLISECOND, TOP_OF_SECOND, TOP_OF_MINUTE, TOP_OF_HOUR, HALF_DAY, TOP_OF_DAY, TOP_OF_WEEK, TOP_OF_MONTH;

    // The followed list consists of valid periodicy types in increasing period
    // lengths
    static PeriodicityType[] VALID_ORDERED_LIST = new PeriodicityType[] { TOP_OF_MILLISECOND, PeriodicityType.TOP_OF_SECOND, PeriodicityType.TOP_OF_MINUTE,
            PeriodicityType.TOP_OF_HOUR, PeriodicityType.TOP_OF_DAY, PeriodicityType.TOP_OF_WEEK, PeriodicityType.TOP_OF_MONTH };

}
