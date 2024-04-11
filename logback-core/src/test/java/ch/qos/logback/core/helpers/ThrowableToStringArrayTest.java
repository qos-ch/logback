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
package ch.qos.logback.core.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.CoreConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThrowableToStringArrayTest {

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    public void verify(Throwable t) {
        t.printStackTrace(pw);

        String[] sa = ThrowableToStringArray.convert(t);
        StringBuilder sb = new StringBuilder();
        for (String tdp : sa) {
            sb.append(tdp);
            sb.append(CoreConstants.LINE_SEPARATOR);
        }
        String expected = sw.toString();
        String result = sb.toString().replace("common frames omitted", "more");
        assertEquals(expected, result);
    }

    @Test
    public void smoke() {
        Exception e = new Exception("smoke");
        verify(e);
    }

    @Test
    public void nested() {
        Exception w = null;
        try {
            someMethod();
        } catch (Exception e) {
            w = new Exception("wrapping", e);
        }
        verify(w);
    }

    @Test
    public void multiNested() {
        Exception w = null;
        try {
            someOtherMethod();
        } catch (Exception e) {
            w = new Exception("wrapping", e);
        }
        verify(w);
    }

    void someMethod() throws Exception {
        throw new Exception("someMethod");
    }

    void someOtherMethod() throws Exception {
        try {
            someMethod();
        } catch (Exception e) {
            throw new Exception("someOtherMethod", e);
        }
    }
}
