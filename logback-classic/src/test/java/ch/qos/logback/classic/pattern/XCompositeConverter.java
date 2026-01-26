/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

import static org.junit.jupiter.api.Assertions.assertNull;

public class XCompositeConverter extends CompositeConverter<ILoggingEvent> {

    void assertNoNext() {
        assertNull( this.getNext(), "converter instance has next element");
    }

    @Override
    protected String transform(ILoggingEvent event, String in) {
        if (event.getMessage().contains("assert"))
            assertNoNext();
        return "";
    }

}
