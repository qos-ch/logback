/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.util;

import static org.junit.Assert.*;

import org.junit.Test;


public class ContentTypeUtilTest {

  
  @Test
  public void smoke() {
    String contextType = "text/html";
    assertTrue(ContentTypeUtil.isTextual(contextType));
    assertEquals("html", ContentTypeUtil.getSubType(contextType));
  }
  
  @Test
  public void nullContext() {
    String contextType = null;
    assertFalse(ContentTypeUtil.isTextual(contextType));
    assertNull(ContentTypeUtil.getSubType(contextType));
  }
  
  @Test
  public void emptySubtype() {
    String contextType = "text/";
    assertTrue(ContentTypeUtil.isTextual(contextType));
    assertNull(ContentTypeUtil.getSubType(contextType));
  }
}
