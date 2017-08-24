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
package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CallerDataTest {

    @Test
    public void testBasic() {
        Throwable t = new Throwable();
        StackTraceElement[] steArray = t.getStackTrace();

        StackTraceElement[] cda = CallerData.extract(t, CallerDataTest.class.getName(), 50, null);
        assertNotNull(cda);
        assertTrue(cda.length > 0);
        assertEquals(steArray.length - 1, cda.length);
    }

    /**
     * This test verifies that in case caller data cannot be
     * extracted, CallerData.extract does not throw an exception
     *
     */
    @Test
    public void testDeferredProcessing() {
        StackTraceElement[] cda = CallerData.extract(new Throwable(), "com.inexistent.foo", 10, null);
        assertNotNull(cda);
        assertEquals(0, cda.length);
    }

}
