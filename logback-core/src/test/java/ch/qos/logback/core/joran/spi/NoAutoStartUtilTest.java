/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class NoAutoStartUtilTest {

  
  @Test
  public void commonObject() {
    Object o = new Object();
    assertTrue(NoAutoStartUtil.notMarkedWithNoAutoStart(o));
  }
  
  @Test
  public void markedWithNoAutoStart() {
    DoNotAutoStart o = new DoNotAutoStart();
    assertFalse(NoAutoStartUtil.notMarkedWithNoAutoStart(o));
  }
}
