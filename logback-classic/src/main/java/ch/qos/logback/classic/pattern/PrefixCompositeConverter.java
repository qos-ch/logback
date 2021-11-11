/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2021, QOS.ch. All rights reserved.
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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import ch.qos.logback.core.pattern.Converter;

public class PrefixCompositeConverter extends CompositeConverter<ILoggingEvent> {

    @Override
    public String convert(final ILoggingEvent event) {
        final StringBuilder buf = new StringBuilder();
        final Converter<ILoggingEvent> childConverter = getChildConverter();

        for (Converter<ILoggingEvent> c = childConverter; c != null; c = c.getNext()) {
            if (c instanceof MDCConverter) {
                final MDCConverter mdcConverter = (MDCConverter) c;

                final String key = mdcConverter.getKey();
                if (key != null) {
                    buf.append(key).append("=");
                }
            } else if (c instanceof PropertyConverter) {
                final PropertyConverter pc = (PropertyConverter) c;
                final String key = pc.getKey();
                if (key != null) {
                    buf.append(key).append("=");
                }
            } else {
                final String classOfConverter = c.getClass().getName();

                final String key = PatternLayout.CONVERTER_CLASS_TO_KEY_MAP.get(classOfConverter);
                if (key != null) {
                    buf.append(key).append("=");
                }
            }
            buf.append(c.convert(event));
        }
        return buf.toString();
    }

    @Override
    protected String transform(final ILoggingEvent event, final String in) {
        throw new UnsupportedOperationException();
    }
}
