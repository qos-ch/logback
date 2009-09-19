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

import java.io.StringWriter;

import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.layout.NopLayout;


public class DummyAppenderTest {

  
  protected Appender<Object> getAppender() {
    return new DummyAppender<Object>(new StringWriter());
  }
  
  protected Appender<Object> getConfiguredAppender() {
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
