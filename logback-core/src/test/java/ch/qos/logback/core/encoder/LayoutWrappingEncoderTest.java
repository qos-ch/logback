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
package ch.qos.logback.core.encoder;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LayoutWrappingEncoderTest {

    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);
    LayoutWrappingEncoder<Object> encoder = new LayoutWrappingEncoder<Object>();

    static class EchoLayout extends LayoutBase<Object> {
        public String doLayout(Object event) {
            return String.valueOf(event);
        }
    }

    @BeforeEach
    public void setUp() {
        encoder.setContext(context);
    }

    // https://github.com/qos-ch/logback/issues/1046
    // A wrapping encoder left without a layout (e.g. when an <if>/<then>/<else>
    // block is silently ignored) used to start "successfully" and then throw a
    // NullPointerException on every logging event, causing silent log loss.
    @Test
    public void nullLayoutReportsErrorOnStartAndDoesNotThrowOnEncode() {
        encoder.start();
        statusChecker.assertContainsMatch(Status.ERROR, "No layout set for the encoder");

        byte[] result = assertDoesNotThrow(() -> encoder.encode(new Object()));
        assertNull(result);
    }

    @Test
    public void encodesWhenLayoutIsSet() {
        encoder.setLayout(new EchoLayout());
        encoder.start();
        statusChecker.assertIsErrorFree();

        byte[] result = encoder.encode("hello");
        assertEquals("hello", new String(result));
    }
}
