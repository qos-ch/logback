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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;

public class StatusPrinterTest {

    ByteArrayOutputStream outputStream;
    PrintStream ps;

    @Before
    public void setUp() throws Exception {
        outputStream = new ByteArrayOutputStream();
        ps = new PrintStream(outputStream);
        StatusPrinter.setPrintStream(ps);
    }

    @After
    public void tearDown() throws Exception {
        StatusPrinter.setPrintStream(System.out);
        ps = null;
        outputStream = null;
    }

    @Test
    public void testBasic() {
        final Context context = new ContextBase();
        context.getStatusManager().add(new InfoStatus("test", this));
        StatusPrinter.print(context);
        final String result = outputStream.toString();
        assertTrue(result.contains("|-INFO in " + this.getClass().getName()));
    }

    @Test
    public void testNested() {
        final Status s0 = new ErrorStatus("test0", this);
        final Status s1 = new InfoStatus("test1", this);
        final Status s11 = new InfoStatus("test11", this);
        final Status s12 = new InfoStatus("test12", this);
        s1.add(s11);
        s1.add(s12);

        final Status s2 = new InfoStatus("test2", this);
        final Status s21 = new InfoStatus("test21", this);
        final Status s211 = new WarnStatus("test211", this);

        final Status s22 = new InfoStatus("test22", this);
        s2.add(s21);
        s2.add(s22);
        s21.add(s211);

        final Context context = new ContextBase();
        context.getStatusManager().add(s0);
        context.getStatusManager().add(s1);
        context.getStatusManager().add(s2);

        StatusPrinter.print(context);
        final String result = outputStream.toString();
        assertTrue(result.contains("+ INFO in " + this.getClass().getName()));
        assertTrue(result.contains("+ WARN in " + this.getClass().getName()));
        assertTrue(result.contains("    |-WARN in " + this.getClass().getName()));
    }

    @Test
    public void testWithException() {
        final Status s0 = new ErrorStatus("test0", this);
        final Status s1 = new InfoStatus("test1", this, new Exception("testEx"));
        final Status s11 = new InfoStatus("test11", this);
        final Status s12 = new InfoStatus("test12", this);
        s1.add(s11);
        s1.add(s12);

        final Status s2 = new InfoStatus("test2", this);
        final Status s21 = new InfoStatus("test21", this);
        final Status s211 = new WarnStatus("test211", this);

        final Status s22 = new InfoStatus("test22", this);
        s2.add(s21);
        s2.add(s22);
        s21.add(s211);

        final Context context = new ContextBase();
        context.getStatusManager().add(s0);
        context.getStatusManager().add(s1);
        context.getStatusManager().add(s2);
        StatusPrinter.print(context);
        final String result = outputStream.toString();
        assertTrue(result.contains("|-ERROR in " + this.getClass().getName()));
        assertTrue(result.contains("+ INFO in " + this.getClass().getName()));
        assertTrue(result.contains("ch.qos.logback.core.util.StatusPrinterTest.testWithException"));
    }

}
