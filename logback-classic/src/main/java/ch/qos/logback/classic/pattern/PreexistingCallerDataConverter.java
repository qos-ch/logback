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


import ch.qos.logback.core.CoreConstants;

import static ch.qos.logback.classic.pattern.CallerDataConverter.DEFAULT_CALLER_LINE_PREFIX;

/**
 * This converter outputs caller data if it exists already.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.6.4
 */
public class PreexistingCallerDataConverter extends ClassicConverter {

    @Override
    public String convert(ch.qos.logback.classic.spi.ILoggingEvent event) {

        boolean hasCallerData = event.hasCallerData();
        if(!hasCallerData) {
            return CoreConstants.EMPTY_STRING;
        }

        StringBuilder buf = new StringBuilder();
        StackTraceElement[] cda = event.getCallerData();
        if (cda != null) {
            for (int i = 0; i < cda.length; i++) {
                buf.append(getCallerLinePrefix());
                buf.append(i);
                buf.append("\t at ");
                buf.append(cda[i]);
                buf.append(CoreConstants.LINE_SEPARATOR);
            }
            return buf.toString();
        } else {
            return CoreConstants.EMPTY_STRING;
        }
    }

    protected String getCallerLinePrefix() {
        return DEFAULT_CALLER_LINE_PREFIX;
    }
}
