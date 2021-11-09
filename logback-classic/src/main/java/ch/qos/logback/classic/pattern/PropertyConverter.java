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
package ch.qos.logback.classic.pattern;

import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;

public final class PropertyConverter extends ClassicConverter {

    String key;

    @Override
    public void start() {
        final String optStr = getFirstOption();
        if (optStr != null) {
            key = optStr;
            super.start();
        }
    }

    public String getKey() {
        return key;
    }

    @Override
    public String convert(final ILoggingEvent event) {
        if (key == null) {
            return "Property_HAS_NO_KEY";
        }
        final LoggerContextVO lcvo = event.getLoggerContextVO();
        final Map<String, String> map = lcvo.getPropertyMap();
        final String val = map.get(key);
        if (val != null) {
            return val;
        }
        return System.getProperty(key);
    }
}
