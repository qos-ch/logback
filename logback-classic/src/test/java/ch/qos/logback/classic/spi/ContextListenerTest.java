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
package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.BasicContextListener.UpdateType;

public class ContextListenerTest  {
  
  LoggerContext context;
  BasicContextListener listener;
  
  @Before
  public void setUp() throws Exception {
    context = new LoggerContext();
    listener = new BasicContextListener();
    context.addListener(listener);
  }

  @Test
  public void testNotifyOnReset() {
    context.reset();
    assertEquals(UpdateType.RESET, listener.updateType);
    assertEquals(listener.context, context);
  }

  @Test
  public void testNotifyOnStopResistant() {
    listener.setResetResistant(true);
    context.stop();
    assertEquals(UpdateType.STOP, listener.updateType);
    assertEquals(listener.context, context);
  }

  @Test
  public void testNotifyOnStopNotResistant() {
    context.stop();
    assertEquals(UpdateType.RESET, listener.updateType);
    assertEquals(listener.context, context);
  }

  @Test
  public void testNotifyOnStart() {
    context.start();
    assertEquals(UpdateType.START, listener.updateType);
    assertEquals(listener.context, context);
  }
}
