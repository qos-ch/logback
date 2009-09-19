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
package ch.qos.logback.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

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
}
