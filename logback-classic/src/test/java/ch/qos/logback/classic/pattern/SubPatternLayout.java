/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

import ch.qos.logback.classic.PatternLayout;

import java.util.Map;

/**
 * Test backward compatibility support by virtue of correct compilation.
 *
 * See also SubPatternLayoutTest
 */
public class SubPatternLayout extends PatternLayout {

    static String DOOO = "dooo";

    SubPatternLayout() {
        Map<String, String> defaultConverterMap = getDefaultConverterMap();
        defaultConverterMap.put(DOOO, DateConverter.class.getName());
    }

}
