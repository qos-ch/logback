/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

import junit.framework.TestCase;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.pattern.ExceptionalConverter;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;


abstract public class AbstractPatternLayoutBaseTest extends TestCase {

  public AbstractPatternLayoutBaseTest(String arg0) {
    super(arg0);
  }

  abstract public PatternLayoutBase getPatternLayoutBase();
  abstract public Object getEventObject();
  abstract public Context getContext();
  
  public void testUnStarted() {
    PatternLayoutBase plb = getPatternLayoutBase();
    Context context = new ContextBase();
    plb.setContext(context);
    String s = plb.doLayout(getEventObject());
    assertEquals("", s);
    StatusManager sm = context.getStatusManager();
    StatusPrinter.print(sm);
  }

  /**
   * This test checks that the pattern layout implementation starts its
   * converters. ExceptionalConverter throws an exception if it's convert
   * method is called before being started.
   */
  public void testConverterStart() {
    PatternLayoutBase plb = getPatternLayoutBase();
    plb.setContext(getContext());
    plb.getInstanceConverterMap().put("EX", ExceptionalConverter.class.getName());
    plb.setPattern("%EX");
    plb.start();
    String result = plb.doLayout(getEventObject());
    assertFalse(result.contains("%PARSER_ERROR_EX"));
    //System.out.println("========="+result);
  }

  public void testStarted() {
    PatternLayoutBase plb = getPatternLayoutBase();
    Context context = new ContextBase();
    plb.setContext(context);
    String s = plb.doLayout(getEventObject());
    assertEquals("", s);
    StatusManager sm = context.getStatusManager();
    StatusPrinter.print(sm);
  }

  public void testNullPattern() {
    //System.out.println("testNullPattern");
    PatternLayoutBase plb = getPatternLayoutBase();
    Context context = new ContextBase();
    plb.setContext(context);
    plb.start();
    String s = plb.doLayout(getEventObject());
    assertEquals("", s);
    StatusChecker checker = new StatusChecker(context.getStatusManager());
    //StatusPrinter.print(context.getStatusManager());
    assertTrue(checker.containsMatch("Failed to parse pattern \"null\""));
  }

}
