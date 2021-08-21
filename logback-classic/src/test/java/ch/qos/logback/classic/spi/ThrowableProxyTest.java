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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.util.EnvUtil;

public class ThrowableProxyTest {

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    // compares Throwable.printStackTrace with output by ThrowableProxy
    public void verify(Throwable t) {
        t.printStackTrace(pw);

        IThrowableProxy tp = new ThrowableProxy(t);

        String result = ThrowableProxyUtil.asString(tp);
        result = result.replace("common frames omitted", "more");
        String expected = sw.toString();

        //System.out.println("========expected");
        //System.out.println(expected);

        //System.out.println("========result");
        //System.out.println(result);

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
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            e.addSuppressed(fooException);
            e.addSuppressed(barException);
            
            ex = e;
        }
        verify(ex);
    }

    @Test
    public void suppressedWithCause() throws InvocationTargetException, IllegalAccessException {
                                                      // sense.
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper", e);
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            
            ex.addSuppressed(fooException);
            e.addSuppressed(barException);
            
        }
        verify(ex);
    }

    @Test
    public void suppressedWithSuppressed() throws Exception {
        Exception ex = null;
        try {
            someMethod();
        } catch (Exception e) {
            ex = new Exception("Wrapper", e);
            Exception fooException = new Exception("Foo");
            Exception barException = new Exception("Bar");
            barException.addSuppressed(fooException);
            e.addSuppressed(barException);
  
        }
        verify(ex);
    }

    // see also https://jira.qos.ch/browse/LOGBACK-453
    @Test
    public void nullSTE() {
        Throwable t = new Exception("someMethodWithNullException") {
            private static final long serialVersionUID = 1L;

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

    // see also https://jira.qos.ch/browse/LOGBACK-1454
    @Test
    public void cyclicCause() {
    	// Earlier JDKs may formats things differently
    	if(!EnvUtil.isJDK16OrHigher())
    		return;
        Exception e = new Exception("foo");
        Exception e2 = new Exception(e);
        e.initCause(e2);
        verify(e);
    }

    // see also https://jira.qos.ch/browse/LOGBACK-1454
    @Test
    public void cyclicSuppressed() {
    	// Earlier JDKs may formats things differently
    	if(!EnvUtil.isJDK16OrHigher())
    		return;
        Exception e = new Exception("foo");
        Exception e2 = new Exception(e);
        e.addSuppressed(e2);
        verify(e);
    }

    void someMethod() throws Exception {
        throw new Exception("someMethod");
    }

    void someMethodWithNullException() throws Exception {
        throw new Exception("someMethodWithNullException") {
            private static final long serialVersionUID = -2419053636101615373L;

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
