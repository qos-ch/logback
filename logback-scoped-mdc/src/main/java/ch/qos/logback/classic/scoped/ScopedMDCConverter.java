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
package ch.qos.logback.classic.scoped;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Map;

import static ch.qos.logback.core.util.OptionHelper.extractDefaultReplacement;

/**
 * A pattern converter that reads values from {@link ScopedMDC}.
 *
 * <p>Register in logback.xml:</p>
 * <pre>
 * &lt;conversionRule conversionWord="scopedContext"
 *     converterClass="ch.qos.logback.classic.scoped.ScopedMDCConverter" /&gt;
 * &lt;conversionRule conversionWord="Y"
 *     converterClass="ch.qos.logback.classic.scoped.ScopedMDCConverter" /&gt;
 * </pre>
 *
 * <p>Then use {@code %scopedContext} or {@code %Y} in patterns.
 * Use {@code %scopedContext{key}} for a specific key, or
 * {@code %scopedContext{key:-default}} for a key with a default value.</p>
 *
 * <p><b>Note:</b> This converter reads from the thread-current {@link ScopedValue}
 * binding at format time. It works correctly with synchronous appenders. With
 * asynchronous appenders, the scoped values will not be available on the
 * formatting thread.</p>
 *
 * @since 1.5.33
 */
public class ScopedMDCConverter extends ClassicConverter {

    private String key;
    private String defaultValue = "";

    @Override
    public void start() {
        String[] keyInfo = extractDefaultReplacement(getFirstOption());
        key = keyInfo[0];
        if (keyInfo[1] != null) {
            defaultValue = keyInfo[1];
        }
        super.start();
    }

    @Override
    public void stop() {
        key = null;
        super.stop();
    }

    @Override
    public String convert(ILoggingEvent event) {
        Map<String, String> scopedMap = ScopedMDC.getPropertyMap();

        if (scopedMap.isEmpty()) {
            return defaultValue;
        }

        if (key == null) {
            return outputForAllKeys(scopedMap);
        } else {
            String value = scopedMap.get(key);
            if (value != null) {
                return value;
            } else {
                return defaultValue;
            }
        }
    }

    private String outputForAllKeys(Map<String, String> scopedMap) {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : scopedMap.entrySet()) {
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            buf.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return buf.toString();
    }

    public String getKey() {
        return key;
    }
}
