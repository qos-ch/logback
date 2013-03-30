/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.read.ListAppender;

public class AppenderTrackerTest {

  
  Context context = new ContextBase();
  AppenderTracker<Object> appenderTracker = new AppenderTrackerImpl<Object>();
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
    appenderTracker.stopStaleAppenders(now);
    assertEquals(0, appenderTracker.keyList().size());
  }
  
  @Test
  public void empty1() {
    long now = 3000;
    assertNull(appenderTracker.get(key, now++));
    now += AppenderTracker.DEFAULT_TIMEOUT+1000;
    appenderTracker.stopStaleAppenders(now);
    assertNull(appenderTracker.get(key, now++));
  }
  
  @Test
  public void smoke() {
    assertTrue(la.isStarted());
    long now = 3000;
    appenderTracker.put(key, la, now);
    assertEquals(la, appenderTracker.get(key, now++));
    now += AppenderTracker.DEFAULT_TIMEOUT+1000;
    appenderTracker.stopStaleAppenders(now);
    assertFalse(la.isStarted());
    assertNull(appenderTracker.get(key, now++));
  }
 
  @Test
  public void removeNow() {
    long now = 3000;
    appenderTracker.put(key, la, now);
    appenderTracker.stopAndRemoveNow(key);
    assertFalse(la.isStarted());
    appenderTracker.get(key, now++);
    assertNull(appenderTracker.get(key, now++));
  }

  @Test
  public void maxAppenders() {
    long now = 3000;
    List<Appender<Object>> appenderList = new ArrayList<Appender<Object>>();
    appenderTracker.setMaxAppenders(10);
    for (int i = 0; i < 11; i++) {
      Appender<Object> appender = new ListAppender<Object>();
      appender.start();
      appenderList.add(appender);
      appenderTracker.put(key + i, appender, now++);
    }
    assertNull(appenderTracker.get(key + 0, now++));
    assertFalse(appenderList.get(0).isStarted());
  }
}
