/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.appender;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import junit.framework.Test;
import junit.framework.TestSuite;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.layout.NopLayout;
import ch.qos.logback.core.util.TeeOutputStream;


public class ConsoleAppenderTest extends AbstractAppenderTest<Object> {

  TeeOutputStream tee;
  PrintStream original;

  public ConsoleAppenderTest(String arg) {
    super(arg);

  }

  protected void setUp() throws Exception {
    original = System.out;
    // tee will output bytes on System out but it will also
    // collect them so that the output can be compared against
    // some expected output data
    // tee = new TeeOutputStream(original);
    
    // keep the console quiet
    tee = new TeeOutputStream(null);
    
    // redirect System.out to tee
    System.setOut(new PrintStream(tee));
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setOut(original);
  }

  @Override
  protected AppenderBase<Object> getAppender() {
    return new ConsoleAppender<Object>();
  } 

  protected AppenderBase<Object> getConfiguredAppender() {
    ConsoleAppender<Object> ca = new ConsoleAppender<Object>();
    ca.setLayout(new NopLayout<Object>());
    ca.start();
    return ca;
  }

  public void testBasic() {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    ca.setLayout(new DummyLayout<Object>());
    ca.start();
    ca.doAppend(new Object());
    assertEquals(DummyLayout.DUMMY, tee.toString());
  }
  
  public void testOpen() {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    DummyLayout<Object> dummyLayout = new DummyLayout<Object>();
    dummyLayout.setFileHeader("open");
    ca.setLayout(dummyLayout);
    ca.start();
    ca.doAppend(new Object());
    ca.stop();
    assertEquals("open"+Layout.LINE_SEP+DummyLayout.DUMMY, tee.toString());
  }
  public void testClose() {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    DummyLayout<Object> dummyLayout = new DummyLayout<Object>();
    dummyLayout.setFileFooter("closed");
    ca.setLayout(dummyLayout);
    ca.start();
    ca.doAppend(new Object());
    ca.stop();
    assertEquals(DummyLayout.DUMMY + "closed", tee.toString());
  }



  
  public void testUTF16BE() throws UnsupportedEncodingException {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    ca.setLayout(new DummyLayout<Object>());
    String encodingName = "UTF-16BE";
    ca.setEncoding(encodingName);
    ca.start();
    ca.doAppend(new Object());

    assertEquals(DummyLayout.DUMMY, new String(tee.toByteArray(), encodingName));
  }

  public static Test xxsuite() {
    TestSuite suite = new TestSuite();
    //suite.addTest(new ConsoleAppenderTest("testOpen"));
    suite.addTestSuite(ConsoleAppenderTest.class);
    return suite;
  }
}
