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
package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.PostCompileProcessor;

public class EnsureLineSeparation implements PostCompileProcessor<IAccessEvent> {

    /**
     * Add a line separator converter so that access event appears on a separate
     * line.
     */
    @Override
    public void process(Context context, Converter<IAccessEvent> head) {
        if (head == null)
            throw new IllegalArgumentException("Empty converter chain");

        // if head != null, then tail != null as well
        Converter<IAccessEvent> tail = ConverterUtil.findTail(head);
        Converter<IAccessEvent> newLineConverter = new LineSeparatorConverter();
        if (!(tail instanceof LineSeparatorConverter)) {
            tail.setNext(newLineConverter);
        }
    }
}
