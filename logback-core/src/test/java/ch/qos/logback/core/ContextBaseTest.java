/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ch.qos.logback.core.spi.LifeCycle;

public class ContextBaseTest {

  ContextBase context = new ContextBase();

  @Test
  public void renameDefault() {
    context.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
    context.setName("hello");
  }


  @Test
  public void idempotentNameTest() {
    context.setName("hello");
    context.setName("hello");
  }

  @Test
  public void renameTest() {
    context.setName("hello");
    try {
      context.setName("x");
      fail("renaming is not allowed");
    } catch (IllegalStateException ise) {
    }
  }

  @Test
  public void resetTest() {
    context.setName("hello");
    context.putProperty("keyA", "valA");
    context.putObject("keyA", "valA");
    assertEquals("valA", context.getProperty("keyA"));
    assertEquals("valA", context.getObject("keyA"));
    context.reset();
    assertNull(context.getProperty("keyA"));
    assertNull(context.getObject("keyA"));
  }

  @Test
  public void contextNameProperty() {
    assertNull(context.getProperty(CoreConstants.CONTEXT_NAME_KEY));
    String HELLO = "hello";
    context.setName(HELLO);
    assertEquals(HELLO, context.getProperty(CoreConstants.CONTEXT_NAME_KEY));
    // good to have a raw reference to the "CONTEXT_NAME" as most clients would
    // not go through CoreConstants
    assertEquals(HELLO, context.getProperty("CONTEXT_NAME"));
  }
  
  @Test
  public void addLifeCycleComponentTest() {
    MockLifeCycleComponent component = new MockLifeCycleComponent();
    context.addLifeCycleComponent(component);
    assertTrue(component.isStarted());
    context.reset();
    assertFalse(component.isStarted());
  }

  private static class MockLifeCycleComponent implements LifeCycle {

    private boolean started;
    
    public void start() {
      started = true;      
    }

    public void stop() {
      started = false;
    }

    public boolean isStarted() {
      return started;
    }
    
  }
  
}
