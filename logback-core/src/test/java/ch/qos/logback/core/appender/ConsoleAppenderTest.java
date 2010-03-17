/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.appender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.DummyEncoder;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.NopEncoder;
import ch.qos.logback.core.layout.DummyLayout;


public class ConsoleAppenderTest extends AbstractAppenderTest<Object> {

  XTeeOutputStream tee;
  PrintStream original;


  @Before
  public void setUp() throws Exception {
    original = System.out;
    // tee will output bytes on System out but it will also
    // collect them so that the output can be compared against
    // some expected output data
    // tee = new TeeOutputStream(original);
    
    // keep the console quiet
    tee = new XTeeOutputStream(null);
    
    // redirect System.out to tee
    System.setOut(new PrintStream(tee));
  }

  @After
  public void tearDown() throws Exception {
    System.setOut(original);
  }

  @Override
  public Appender<Object> getAppender() {
    return new ConsoleAppender<Object>();
  } 

  protected Appender<Object> getConfiguredAppender() {
    ConsoleAppender<Object> ca = new ConsoleAppender<Object>();
    ca.setEncoder(new NopEncoder<Object>());
    ca.start();
    return ca;
  }

  @org.junit.Test
  public void testBasic() {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    ca.setEncoder(new DummyEncoder<Object>());
    ca.start();
    ca.doAppend(new Object());
    assertEquals(DummyLayout.DUMMY, tee.toString());
  }
  
  @org.junit.Test
  public void testOpen() {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    DummyEncoder<Object> dummyEncoder = new DummyEncoder<Object>();
    dummyEncoder.setFileHeader("open");
    ca.setEncoder(dummyEncoder);
    ca.start();
    ca.doAppend(new Object());
    ca.stop();
    assertEquals("open"+CoreConstants.LINE_SEPARATOR+DummyLayout.DUMMY, tee.toString());
  }
  
  @Test
  public void testClose() {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    DummyEncoder<Object> dummyEncoder = new DummyEncoder<Object>();
    dummyEncoder.setFileFooter("CLOSED");
    ca.setEncoder(dummyEncoder);
    ca.start();
    ca.doAppend(new Object());
    ca.stop();
    // ConsoleAppender must keep the underlying stream open.
    // The console is not ours to close.
    assertFalse(tee.isClosed());
    assertEquals(DummyLayout.DUMMY + "CLOSED", tee.toString());
  }

  // See http://jira.qos.ch/browse/LBCORE-143
  @Test
  public void changeInConsole() {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    EchoEncoder<Object> encoder = new EchoEncoder<Object>();
    ca.setEncoder(encoder);
    ca.start();
    ca.doAppend("a");
    assertEquals("a"+CoreConstants.LINE_SEPARATOR, tee.toString());
    
    XTeeOutputStream newTee = new XTeeOutputStream(null);
    System.setOut(new PrintStream(newTee));
    ca.doAppend("b");
    assertEquals("b"+CoreConstants.LINE_SEPARATOR, newTee.toString());
  }


  @Test  
  public void testUTF16BE() throws UnsupportedEncodingException {
    ConsoleAppender<Object> ca = (ConsoleAppender<Object>) getAppender();
    DummyEncoder<Object> dummyEncoder = new DummyEncoder<Object>();
    String encodingName = "UTF-16BE";
    dummyEncoder.setEncodingName(encodingName);
    ca.setEncoder(dummyEncoder);
    ca.start();
    ca.doAppend(new Object());
    assertEquals(DummyLayout.DUMMY, new String(tee.toByteArray(), encodingName));
  }


}
