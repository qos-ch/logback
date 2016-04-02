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
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ch.qos.logback.classic.util.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ThrowableProxyTest {

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public void verify(Throwable t) {
        t.printStackTrace(pw);

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
    public void multiNested() {
        Exception w = null;
        try {
            someOtherMethod();
        } catch (Exception e) {
            w = new Exception("wrapping", e);
        }
        verify(w);
    }
    
    @Test
    public void nullMessage() {
        /*
        Ensures that a null message is not sent to the output.
        */
        Exception t = new Exception();
        verify(t);
    }

    @Test
    public void circular() {
        Exception w = null;
        try {
            someMethod();
        } catch (Exception e) {
            w = new Exception("Wrapper", e);
            e.initCause(w);
        }
        verify(w);
    }

    @Test
    public void suppressedCircular() throws Exception {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
           
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper");
            addSuppressed(ex, e);
            addSuppressed(e, ex);
        }
        
        verify(ex);
    }

    @Test
    public void suppressedWithCauseCircular() throws InvocationTargetException, IllegalAccessException {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper", e);
            Exception fooException = new Exception("Foo", ex);
            addSuppressed(e, fooException);
            addSuppressed(fooException, e);
        }
        verify(ex);
    }

    @Test
    public void suppressedWithSuppressedCircular() throws Exception {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper");
            addSuppressed(ex, e);
            Exception fooException = new Exception("Foo");
            addSuppressed(e, fooException);
            addSuppressed(fooException, ex);
        }
        verify(ex);
    }

    @Test
    public void circularSuppressedBeforeCause() throws Exception {
        assumeTrue(TestHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make
                                                      // sense.
        
        /*
        Tests to ensure that the suppressed exceptions are processed before the cause, making any circular
        exceptions that appear in both to be classified as "circular" in the cause, not the suppressed chain.
        */
        Exception a = new Exception("a");
        Exception b = new Exception("b");
        Exception c = new Exception("c");
        Exception d = new Exception("d");
        
        a.initCause(c);
        c.initCause(d);
        d.initCause(b);
        
        addSuppressed(a, b);
        addSuppressed(b, d);
        
        verify(a);
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
}
