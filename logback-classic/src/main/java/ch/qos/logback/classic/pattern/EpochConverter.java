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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * The EpochConverter class extends the ClassicConverter to handle the conversion of logging event
 * timestamps into epoch time. This class allows control over whether the output epoch time is
 * represented in milliseconds (default) or seconds.
 *
 * @since 1.5.25
 */
public class EpochConverter extends ClassicConverter {

    // assume output in milliseconds by default
    boolean inUnitsOfSeconds = false;

    @Override
    public void start() {
        String millisOrSecondsStr = getFirstOption();
        if ("seconds".equalsIgnoreCase(millisOrSecondsStr)) {
            inUnitsOfSeconds = true;
        }

        super.start();

    }

    @Override
    public String convert(ILoggingEvent event) {

        if(inUnitsOfSeconds) {
            return Long.toString(event.getTimeStamp() / 1000);
        } else {
            return Long.toString(event.getTimeStamp());
        }
    }
}
