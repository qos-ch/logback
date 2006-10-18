/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.event;

import java.io.FileInputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.Constants;

/**
 * Test whether SaxEventRecorder does a good job.
 * 
 * @author Ceki Gulcu
 */
public class EventRecorderTest extends TestCase {

  Context context =  new ContextBase();

  public EventRecorderTest(String name) {
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
  
  public List<SaxEvent> doTest(String filename) throws Exception {
    SaxEventRecorder recorder = new SaxEventRecorder();
    recorder.setContext(context);
    FileInputStream fis = new FileInputStream(Constants.TEST_DIR_PREFIX
        + "input/joran/"+ filename);
    recorder.recordEvents(fis);
    return  recorder.getSaxEventList();
    
 
  }
 
  public void dump(List<SaxEvent> seList) {
    for (SaxEvent se : seList) {
      System.out.println(se);
    }
  }
  /**
   * Tests that whenever an action throws an exception, processing of child
   * elements is skipped.
   * 
   * @throws Exception
   */
  public void test1() throws Exception {
    List<SaxEvent> seList = doTest("event1.xml");
    StatusManager sm = context.getStatusManager();
    assertTrue(sm.getLevel() == Status.INFO);
    //dump(seList);  
    assertEquals(11, seList.size());
    
  }

  public void test2() throws Exception {
    List<SaxEvent> seList = doTest("ampEvent.xml");
    StatusManager sm = context.getStatusManager();
    assertTrue(sm.getLevel() == Status.INFO);
    //dump(seList);  
    assertEquals(3, seList.size());
    
    BodyEvent be = (BodyEvent) seList.get(1);
    assertEquals("xxx & yyy", be.getText());
  }

  public void test3() throws Exception {
    List<SaxEvent> seList = doTest("inc.xml");
    StatusManager sm = context.getStatusManager();
    assertTrue(sm.getLevel() == Status.INFO);
    //dump(seList);  
    assertEquals(4, seList.size());
    
    StartEvent se = (StartEvent) seList.get(1);
    Attributes attr = se.getAttributes();
    assertNotNull(attr);
    assertEquals("1", attr.getValue("increment"));
  }

  public static Test XXXsuite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new EventRecorderTest("test2"));
    // suite.addTestSuite(SkippingInInterpreterTest.class);
    return suite;
  }

}
