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
package ch.qos.logback.core.pattern.parser;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.pattern.Converter123;
import ch.qos.logback.core.pattern.ConverterHello;
import ch.qos.logback.core.pattern.PatternLayoutBase;

public class SamplePatternLayout<E> extends PatternLayoutBase<E> {

    Map<String, String> converterMap = new HashMap<String, String>();

    public SamplePatternLayout() {
        converterMap.put("OTT", Converter123.class.getName());
        converterMap.put("hello", ConverterHello.class.getName());
    }

    public Map<String, String> getDefaultConverterMap() {
        return converterMap;
    }

    public String doLayout(E event) {
        return writeLoopOnConverters(event);
    }

}
