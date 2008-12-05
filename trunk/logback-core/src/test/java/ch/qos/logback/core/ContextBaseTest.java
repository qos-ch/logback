/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core;

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

}
