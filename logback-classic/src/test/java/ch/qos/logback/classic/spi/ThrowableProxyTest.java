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

import static ch.qos.logback.classic.util.TestHelper.addSuppressed;
import static ch.qos.logback.classic.util.TestHelper.getSuppressed;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import ch.qos.logback.classic.util.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ThrowableProxyTest {
    /** Caption  for labeling causative exception stack traces */
    private static final String CAUSE_CAPTION = "Caused by: ";

    /** Caption for labeling suppressed exception stack traces */
    private static final String SUPPRESSED_CAPTION = "Suppressed: ";

    private void verify(Throwable t) throws InvocationTargetException, IllegalAccessException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        printStackTrace(pw, t);

        IThrowableProxy tp = new ThrowableProxy(t);

        String result = ThrowableProxyUtil.asString(tp);
        result = result.replace("common frames omitted", "more");

        String expected = sw.toString();

        System.out.println("========expected");
        System.out.println(expected);

        System.out.println("========result");
        System.out.println(result);

        assertEquals(expected, result);
    }

    private static void printStackTrace(PrintWriter pw, Throwable t) throws InvocationTargetException, IllegalAccessException {
        // Guard against malicious overrides of Throwable.equals by
        // using a Set with identity equality semantics.
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        dejaVu.add(t);

        // Print our stack trace
        pw.println(t);
        StackTraceElement[] trace = t.getStackTrace();
        for (StackTraceElement traceElement : trace)
            pw.println("\tat " + traceElement);

        // Print suppressed exceptions, if any
        for (Throwable se : getSuppressed(t))
            printEnclosedStackTrace(pw, se, trace, SUPPRESSED_CAPTION, "\t", dejaVu);

        // Print cause, if any
        Throwable cause = getNested(t);
        if (cause != null)
            printEnclosedStackTrace(pw, cause, trace, CAUSE_CAPTION, "", dejaVu);
    }

    private static void printEnclosedStackTrace(
            PrintWriter pw, Throwable t, StackTraceElement[] enclosingTrace, String caption, String prefix, Set<Throwable> dejaVu)
            throws InvocationTargetException, IllegalAccessException {
        if (dejaVu.contains(t)) {
            pw.println("\t[CIRCULAR REFERENCE:" + t + "]");
            return;
        }

        dejaVu.add(t);
        // Compute number of frames in common between this and enclosing trace
        StackTraceElement[] trace = t.getStackTrace();
        int m = trace.length - 1;
        int n = enclosingTrace.length - 1;
        while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
            m--;
            n--;
        }
        int framesInCommon = trace.length - 1 - m;

        // Print our stack trace
        pw.println(prefix + caption + t);
        for (int i = 0; i <= m; i++)
            pw.println(prefix + "\tat " + trace[i]);

        if (framesInCommon != 0)
            pw.println(prefix + "\t... " + framesInCommon + " more");

        // Print suppressed exceptions, if any
        for (Throwable se : getSuppressed(t))
            printEnclosedStackTrace(pw, se, trace, SUPPRESSED_CAPTION, prefix + "\t", dejaVu);

        // Print cause, if any
        Throwable cause = getNested(t);
        if (cause != null)
            printEnclosedStackTrace(pw, cause, trace, CAUSE_CAPTION, prefix, dejaVu);
    }

    private static Throwable getNested(Throwable t) {
        if (t instanceof InvocationTargetException) {
            return ((InvocationTargetException) t).getTargetException();
        }
        return t instanceof SQLException ? ((SQLException) t).getNextException() : t.getCause();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void smoke() throws InvocationTargetException, IllegalAccessException {
        Exception e = new Exception("smoke");
        verify(e);
    }

    @Test
    public void nested() throws InvocationTargetException, IllegalAccessException {
        Exception w = null;
        try {
            someMethod();
        } catch (Exception e) {
            w = new Exception("wrapping", e);
        }
        verify(w);
    }

    @Test
    public void suppressed() throws InvocationTargetException, IllegalAccessException {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            addSuppressed(e, fooException);
            addSuppressed(e, barException);
            ex = e;
        }
        verify(ex);
    }

    @Test
    public void suppressedWithCause() throws InvocationTargetException, IllegalAccessException {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper", e);
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            addSuppressed(ex, fooException);
            addSuppressed(e, barException);
        }
        verify(ex);
    }

    @Test
    public void suppressedWithSuppressed() throws Exception {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper", e);
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            addSuppressed(barException, fooException);
            addSuppressed(e, barException);
        }
        verify(ex);
    }

    // see also http://jira.qos.ch/browse/LBCLASSIC-216
    @Test
    public void nullSTE() {
        Throwable t = new Exception("someMethodWithNullException") {
            @Override
            public StackTraceElement[] getStackTrace() {
                return null;
            }
        };
        // we can't test output as Throwable.printStackTrace method uses
        // the private getOurStackTrace method instead of getStackTrace

        // tests ThrowableProxyUtil.steArrayToStepArray
        new ThrowableProxy(t);

        // tests ThrowableProxyUtil.findNumberOfCommonFrames
        Exception top = new Exception("top", t);
        new ThrowableProxy(top);
    }

    @Test
    public void multiNested() throws InvocationTargetException, IllegalAccessException {
        Exception w = null;
        try {
            someOtherMethod();
        } catch (Exception e) {
            w = new Exception("wrapping", e);
        }
        verify(w);
    }

    @Test
    public void nestedSqlException() throws InvocationTargetException, IllegalAccessException {
        SQLException w = null;
        try {
            someMethodSqlException();
        } catch (SQLException e) {
            w = new SQLException("wrapping");
            w.setNextException(e);
        }
        verify(w);
    }

    @Test
    public void multiNestedInvocationTargetException() throws InvocationTargetException, IllegalAccessException {
        InvocationTargetException w = null;
        try {
            someOtherMethod();
        } catch (Exception e) {
            w = new InvocationTargetException(e, "multiNestedInvocationTargetException");
        }
        verify(w);
    }

    void someMethod() throws Exception {
        throw new Exception("someMethod");
    }

    void someMethodWithNullException() throws Exception {
        throw new Exception("someMethodWithNullException") {
            @Override
            public StackTraceElement[] getStackTrace() {
                return null;
            }
        };
    }

    void someOtherMethod() throws Exception {
        try {
            someMethod();
        } catch (Exception e) {
            throw new Exception("someOtherMethod", e);
        }
    }

    void someMethodSqlException() throws SQLException {
        throw new SQLException("someMethodSqlException");
    }
}
