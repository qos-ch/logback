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

import static ch.qos.logback.core.util.OptionHelper.extractDefaultReplacement;

import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class MDCConverter extends ClassicConverter {

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
    public void gcfConvert(ILoggingEvent event, StringBuilder out) {
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();

        if (mdcPropertyMap == null) {
            out.append(defaultValue);
            return;
        }

        if (key == null) {
            outputMDCForAllKeys(mdcPropertyMap, out);
            return;
        } else {

            String value = event.getMDCPropertyMap().get(key);
            if (value != null) {
                out.append(value);
            } else {
                out.append(defaultValue);
            }
        }
    }

    /**
     * if no key is specified, return all the values present in the MDC, in the format "k1=v1, k2=v2, ..."
     */
    private String outputMDCForAllKeys(Map<String, String> mdcPropertyMap,  StringBuilder out) {
        boolean first = true;
        for (Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
            if (first) {
                first = false;
            } else {
                out.append(", ");
            }
            // format: key0=value0, key1=value1
            out.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return out.toString();
    }
}
