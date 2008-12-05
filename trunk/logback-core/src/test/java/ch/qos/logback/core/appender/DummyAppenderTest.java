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

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.layout.NopLayout;


public class DummyAppenderTest {

  
  protected AppenderBase getAppender() {
    return new DummyAppender(new StringWriter());
  }
  
  protected AppenderBase getConfiguredAppender() {
    DummyAppender<Object> da = new DummyAppender<Object>(new StringWriter());
    da.setLayout(new NopLayout<Object>());
    da.start();
    return da;
  }

  @Test
  public void testBasic() {
    StringWriter sw = new StringWriter();
    DummyAppender<Object> da = new DummyAppender<Object>(sw);
    da.setLayout(new DummyLayout<Object>());
    da.start();
    da.doAppend(new Object());
    assertEquals(DummyLayout.DUMMY, sw.getBuffer().toString());
  }
  
}
