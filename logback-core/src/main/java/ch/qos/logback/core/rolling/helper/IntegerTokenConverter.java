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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private String formatString(final int i) {
        final String s = Integer.toString(i);
        final FormatInfo formattingInfo = getFormattingInfo();
        if (formattingInfo == null) {
            return s;
        }
        return IntStream.range(s.length(), formattingInfo.getMin()).mapToObj(k -> "0")
                        .collect(Collectors.joining("", "", s));
    }

    @Override
    public String convert(final Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Null argument forbidden");
        }
        if (o instanceof Integer) {
            return formatString((Integer) o);
        }
        throw new IllegalArgumentException("Cannot convert " + o + " of type" + o.getClass().getName());
    }

    @Override
    public boolean isApplicable(final Object o) {
        return o instanceof Integer;
    }
}
