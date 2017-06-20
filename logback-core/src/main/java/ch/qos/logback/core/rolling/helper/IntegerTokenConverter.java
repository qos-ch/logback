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
import ch.qos.logback.core.pattern.FormatInfo;

/**
 * When asked to convert an integer, <code>IntegerTokenConverter</code> the
 * string value of that integer.
 * 
 * @author Ceki Gulcu
 */
public class IntegerTokenConverter extends DynamicConverter<Object> implements MonoTypedConverter {

    public final static String CONVERTER_KEY = "i";

    public String convert(int i) {
        String s = Integer.toString(i);
        FormatInfo formattingInfo = getFormattingInfo();
        if (formattingInfo == null) {
            return s;
        }
        int min = formattingInfo.getMin();
        StringBuilder sbuf = new StringBuilder();
        for (int j = s.length(); j < min; ++j) {
            sbuf.append('0');
        }
        return sbuf.append(s).toString();
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
