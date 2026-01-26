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

import static org.junit.jupiter.api.Assertions.assertNull;

public class XThrowableHandlingConverter extends ThrowableHandlingConverter {

    void assertNoNext() {
        assertNull(this.getNext(), "has next");
    }

    @Override
    public String convert(ILoggingEvent event) {
        if (event.getMessage().contains("assert"))
            assertNoNext();
        return "";
    }

}
