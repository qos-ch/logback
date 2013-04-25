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
package ch.qos.logback.core.sift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.read.ListAppender;

public class AppenderTrackerTest {

  
  Context context = new ContextBase();
  AppenderTracker<Object> appenderTracker = new AppenderTracker<Object>(context, null);
  ListAppender<Object> la = new ListAppender<Object>();
  String key = "a";
  
  @Before
  public void setUp() {
    la.setContext(context);
    la.start();
  }

  
  @Test
  public void empty0() {
    long now = 3000;
    appenderTracker.removeStaleComponents(now);
    assertEquals(0, appenderTracker.getComponentCount());
  }

}
