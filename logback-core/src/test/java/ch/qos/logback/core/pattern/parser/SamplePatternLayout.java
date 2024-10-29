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
import java.util.function.Supplier;

import ch.qos.logback.core.pattern.*;

public class SamplePatternLayout<E> extends PatternLayoutBase<E> {

    Map<String, Supplier<DynamicConverter>> converterSupplierMap = new HashMap<>();
    Map<String, String> converterMap = new HashMap<>();

    public SamplePatternLayout() {
        converterSupplierMap.put("OTT", Converter123::new);
        converterSupplierMap.put("hello", ConverterHello::new);
    }

    public Map<String, Supplier<DynamicConverter>> getDefaultConverterSupplierMap() {
        return converterSupplierMap;
    }

    @Override
    public Map<String, String> getDefaultConverterMap() {
        return converterMap;
    }

    public String doLayout(E event) {
        return writeLoopOnConverters(event);
    }

}
