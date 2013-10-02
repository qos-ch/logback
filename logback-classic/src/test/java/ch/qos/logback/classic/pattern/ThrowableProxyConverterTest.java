/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.pattern;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.TeztHelper;

import static ch.qos.logback.classic.util.TeztHelper.addSuppressed;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class ThrowableProxyConverterTest {

  LoggerContext lc = new LoggerContext();
  ThrowableProxyConverter tpc = new ThrowableProxyConverter();
  StringWriter sw = new StringWriter();
  PrintWriter pw = new PrintWriter(sw);

  @Before
  public void setUp() throws Exception {
    tpc.setContext(lc);
    tpc.start();
  }

  @After
  public void tearDown() throws Exception {
  }

  private ILoggingEvent createLoggingEvent(Throwable t) {
    return new LoggingEvent(this.getClass().getName(), lc
        .getLogger(Logger.ROOT_LOGGER_NAME), Level.DEBUG, "test message", t,
        null);
  }

  @Test
  public void suppressed() throws InvocationTargetException, IllegalAccessException
  {
    assumeTrue(TeztHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make sense.
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
  public void suppressedWithCause() throws InvocationTargetException, IllegalAccessException
  {
    assumeTrue(TeztHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make sense.
    Exception ex = null;
    try {
      someMethod();
    } catch (Exception e) {
      ex=new Exception("Wrapper", e);
      Exception fooException = new Exception("Foo");
      Exception barException = new Exception("Bar");
      addSuppressed(ex, fooException);
      addSuppressed(e, barException);
    }
    verify(ex);
  }

  @Test
  public void suppressedWithSuppressed() throws Exception
  {
    assumeTrue(TeztHelper.suppressedSupported()); // only execute on Java 7, would work anyway but doesn't make sense.
    Exception ex = null;
    try {
      someMethod();
    } catch (Exception e) {
      ex=new Exception("Wrapper", e);
      Exception fooException = new Exception("Foo");
      Exception barException = new Exception("Bar");
      addSuppressed(barException, fooException);
      addSuppressed(e, barException);
    }
    verify(ex);
  }

  @Test
  public void smoke() {
    Exception t = new Exception("smoke");
    verify(t);
  }

  @Test
  public void nested() {
    Throwable t = TeztHelper.makeNestedException(1);
    verify(t);
  }

  @Test
  public void withArgumentOfOne() throws Exception {
    final Throwable t = TeztHelper.makeNestedException(0);
    t.printStackTrace(pw);
    final ILoggingEvent le = createLoggingEvent(t);

    final List<String> optionList = Arrays.asList("1");
    tpc.setOptionList(optionList);
    tpc.start();

    final String result = tpc.convert(le);

    final BufferedReader reader = new BufferedReader(new StringReader(result));
    assertTrue(reader.readLine().contains(t.getMessage()));
    assertNotNull(reader.readLine());
    assertNull("Unexpected line in stack trace", reader.readLine());
  }

  @Test
  public void withShortArgument() throws Exception {
    final Throwable t = TeztHelper.makeNestedException(0);
    t.printStackTrace(pw);
    final ILoggingEvent le = createLoggingEvent(t);

    final List<String> options = Arrays.asList("short");
    tpc.setOptionList(options);
    tpc.start();

    final String result = tpc.convert(le);

    final BufferedReader reader = new BufferedReader(new StringReader(result));
    assertTrue(reader.readLine().contains(t.getMessage()));
    assertNotNull(reader.readLine());
    assertNull("Unexpected line in stack trace", reader.readLine());
  }

  void someMethod() throws Exception {
    throw new Exception("someMethod");
  }

  void verify(Throwable t) {
    t.printStackTrace(pw);

    ILoggingEvent le = createLoggingEvent(t);
    String result = tpc.convert(le);
    System.out.println(result);
    result = result.replace("common frames omitted", "more");
    assertEquals(sw.toString(), result);
  }
}
