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

    public String convert(ILoggingEvent event) {
        StringBuilder buf = new StringBuilder();
        Converter<ILoggingEvent> childConverter = this.getChildConverter();

        for (Converter<ILoggingEvent> c = childConverter; c != null; c = c.getNext()) {
            if (c instanceof MDCConverter) {
                MDCConverter mdcConverter = (MDCConverter) c;

                String key = mdcConverter.getKey();
                if (key != null) {
                    buf.append(key).append("=");
                } 
            } else if (c instanceof PropertyConverter) {
            	PropertyConverter pc = (PropertyConverter) c;
            	String key = pc.getKey();
            	if (key != null) {
                    buf.append(key).append("=");
                } 
            } else {
            	String classOfConverter = c.getClass().getName();
            	
            	String key = PatternLayout.CONVERTER_CLASS_TO_KEY_MAP.get(classOfConverter);
                if(key != null) 
                	buf.append(key).append("=");
            }
            buf.append(c.convert(event));
        }
        return buf.toString();
    }

    protected String transform(ILoggingEvent event, String in) {
        throw new UnsupportedOperationException();
    }
}

