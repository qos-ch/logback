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
package ch.qos.logback.classic.spi;

import java.util.Comparator;

import ch.qos.logback.classic.Logger;

public class LoggerComparator implements Comparator<Logger> {

    @Override
    public int compare(final Logger l1, final Logger l2) {
        if (l1.getName().equals(l2.getName())) {
            return 0;
        }
        if (l1.getName().equals(org.slf4j.Logger.ROOT_LOGGER_NAME)) {
            return -1;
        }
        if (l2.getName().equals(org.slf4j.Logger.ROOT_LOGGER_NAME)) {
            return 1;
        }
        return l1.getName().compareTo(l2.getName());
    }

}
