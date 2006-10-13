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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEvent;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
import ch.qos.logback.core.util.Constants;
import ch.qos.logback.core.util.StatusPrinter;


/**
 * Test the way Interpreter skips elements in case of exceptions thrown by
 * Actions.
 * 
 * @author Ceki Gulcu
 */
public class EventInterpreterTest extends TestCase {

  public EventInterpreterTest(String name) {
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
  public void test1() throws Exception {
    RuleStore rs = new SimpleRuleStore(new ContextBase());
    
    Interpreter jp = new Interpreter(rs);
    ExecutionContext ec = jp.getExecutionContext();
    ec.setContext(new ContextBase());
    
    SAXParser saxParser = createParser();
    saxParser.parse("file:" + Constants.TEST_DIR_PREFIX + "input/joran/event1.xml", jp);
   
    StatusPrinter.print(ec.getStatusManager());
    for(SaxEvent se : jp.saxEventList) {
      System.out.println(se);
    }

   }

 
//  public static Test suite() {
//    TestSuite suite = new TestSuite();
//    //suite.addTest(new SkippingInInterpreterTest("testSkipSiblings2"));
//    suite.addTestSuite(SkippingInInterpreterTest.class);
//    return suite;
//  }

}
