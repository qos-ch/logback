/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran;

import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.BadBeginAction;
import ch.qos.logback.core.joran.action.BadEndAction;
import ch.qos.logback.core.joran.action.HelloAction;
import ch.qos.logback.core.joran.action.TouchAction;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.Constants;


/**
 * Test the way Interpreter skips elements in case of exceptions thrown by
 * Actions.
 * 
 * @author Ceki Gulcu
 */
public class SkippingInInterpreterTest extends TestCase {

  HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
  Context context = new ContextBase();
  
  public SkippingInInterpreterTest(String name) {
    super(name);
    
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  SAXParser createParser() throws Exception {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    return spf.newSAXParser();
  }

  /**
   * Tests that whenever an action throws an exception, processing of child
   * elements is skipped.
   * 
   * @throws Exception
   */
  public void testChildrenSkipping() throws Exception {
    
    rulesMap.put(new Pattern("test"), new NOPAction());
    rulesMap.put(new Pattern("test/badBegin"), new BadBeginAction());
    rulesMap.put(new Pattern("test/badBegin/touch"), new TouchAction());
    rulesMap.put(new Pattern("test/hello"), new HelloAction());
    
    TrivialConfigurator tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
    
    tc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/exception1.xml");

    String str = context.getProperty("hello");
    assertEquals("Hello John Doe.", str);

    Object i = (String) context.getObject(TouchAction.KEY);
    assertNull(i);
  }

  /**
   * An exception thrown by the end() method of an action will cause sibling
   * elements to be skipped.
   * 
   * @throws Exception
   */
  public void testSkipSiblings() throws Exception {

    rulesMap.put(new Pattern("test"), new NOPAction());
    rulesMap.put(new Pattern("test/badEnd"), new BadEndAction());
    rulesMap.put(new Pattern("test/badEnd/touch"), new TouchAction());
    rulesMap.put(new Pattern("test/hello"), new HelloAction());

    TrivialConfigurator tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
    
    tc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/badEnd1.xml");

    
    String str = context.getProperty("hello");
    assertNull(str);
    Integer i = (Integer) context.getObject(TouchAction.KEY);
    assertEquals(2, i.intValue());
  }

  public void testSkipSiblings2() throws Exception {

    rulesMap.put(new Pattern("test"), new NOPAction());
    rulesMap.put(new Pattern("test/isolate/badEnd"), new BadEndAction());
    rulesMap.put(new Pattern("*/touch"), new TouchAction());
    rulesMap.put(new Pattern("test/hello"), new HelloAction());

    TrivialConfigurator tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
    
    tc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/badEnd2.xml");
    
    String str = context.getProperty("hello");
    assertEquals("Hello John Doe.", str);
    Integer i = (Integer) context.getObject(TouchAction.KEY);
    assertEquals(1, i.intValue());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    //suite.addTest(new SkippingInInterpreterTest("testSkipSiblings2"));
    suite.addTestSuite(SkippingInInterpreterTest.class);
    return suite;
  }

}
