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

import ch.qos.logback.core.pattern.DynamicConverter;

/**
 * When asked to convert an integer, <code>IntegerTokenConverter</code> the
 * string value of that integer.
 * 
 * @author Ceki Gulcu
 */
public class IntegerTokenConverter extends DynamicConverter<Object> implements MonoTypedConverter {

    public final static String CONVERTER_KEY = "i";

    public String convert(int i) {
        return Integer.toString(i);
    }

    public String convert(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Null argument forbidden");
        }
        if (o instanceof Integer) {
            Integer i = (Integer) o;
            return convert(i.intValue());
        }
        throw new IllegalArgumentException("Cannot convert " + o + " of type" + o.getClass().getName());
    }

    public boolean isApplicable(Object o) {
        return (o instanceof Integer);
    }
}
