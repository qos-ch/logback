/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.action.ext.BadBeginAction;
import ch.qos.logback.core.joran.action.ext.HelloAction;
import ch.qos.logback.core.joran.action.ext.TouchAction;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.Constants;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Test the way Interpreter skips elements in case of exceptions thrown by
 * Actions.
 * 
 * @author Ceki Gulcu
 */
public class SkippingInInterpreterTest {

  HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
  Context context = new ContextBase();

  SAXParser createParser() throws Exception {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    return spf.newSAXParser();
  }

  void doTest(String filename) throws Exception {

    rulesMap.put(new Pattern("test"), new NOPAction());
    rulesMap.put(new Pattern("test/badBegin"), new BadBeginAction());
    rulesMap.put(new Pattern("test/badBegin/touch"), new TouchAction());
    rulesMap.put(new Pattern("test/hello"), new HelloAction());

    TrivialConfigurator tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
    tc .doConfigure(Constants.TEST_DIR_PREFIX
            + "input/joran/skip/"+filename);

    String str = context.getProperty("hello");
    assertEquals("Hello John Doe.", str);

    Object i = (String) context.getObject(TouchAction.KEY);
    assertNull(i);

    StatusPrinter.print(context);
  }

  /**
   * Tests that whenever an action throws a RuntimeException, processing of
   * child elements is skipped.
   */
  @Test
  public void testSkippingRuntimeExInBadBegin() throws Exception {
    doTest("badBegin1.xml");
  }

  /**
   * Tests that whenever an action throws a RuntimeException, processing of
   * child elements is skipped.
   */
  @Test
  public void testSkippingActionExInBadBegin() throws Exception {
    doTest("badBegin2.xml");
  }

  /**
   * A RuntimeException thrown by the end() method of an action will be caught without
   * further consequences (as there are no children).
   */
  @Test
  public void testSkippingRuntimeExInBadEnd() throws Exception {
    doTest("badEnd1.xml");
  }

  /**
   * An ActionException thrown by the end() method of an action will be caught without
   * further consequences (as there are no children).
   */
  @Test
  public void testSkippingActionExInBadEnd() throws Exception {
    doTest("badEnd2.xml");
  }
}
