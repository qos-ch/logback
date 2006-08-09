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

import java.io.StringWriter;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.layout.NopLayout;


public class DummyAppenderTest extends AbstractAppenderTest {

  public DummyAppenderTest(String arg) {
    super(arg);
  }
  
  protected AppenderBase getAppender() {
    return new DummyAppender(new StringWriter());
  }
  
  protected AppenderBase getConfiguredAppender() {
    DummyAppender da = new DummyAppender(new StringWriter());
    da.setLayout(new NopLayout());
    da.start();
    return da;
  }

  public void testBasic() {
    StringWriter sw = new StringWriter();
    DummyAppender da = new DummyAppender(sw);
    da.setLayout(new DummyLayout());
    da.start();
    da.doAppend(new Object());
    assertEquals(DummyLayout.DUMMY, sw.getBuffer().toString());
  }
  
}
