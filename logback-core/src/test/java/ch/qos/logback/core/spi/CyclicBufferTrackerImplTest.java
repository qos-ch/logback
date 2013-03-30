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
package ch.qos.logback.core.spi;

import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.sift.AppenderTracker;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class CyclicBufferTrackerImplTest {


  CyclicBufferTrackerImpl<Object> tracker = new CyclicBufferTrackerImpl<Object>();
  String key = "a";

  @Test
  public void empty0() {
    long now = 3000;
    tracker.clearStaleBuffers(now);
    assertEquals(0, tracker.keyList().size());
    assertEquals(0, tracker.bufferCount);
  }

  @Test
  public void empty1() {
    long now = 3000;
    assertNotNull(tracker.getOrCreate(key, now++));
    now += CyclicBufferTracker.THRESHOLD + 1000;
    tracker.clearStaleBuffers(now);
    assertEquals(0, tracker.keyList().size());
    assertEquals(0, tracker.bufferCount);

    assertNotNull(tracker.getOrCreate(key, now++));
  }

  @Test
  public void smoke() {
    long now = 3000;
    CyclicBuffer<Object> cb = tracker.getOrCreate(key, now);
    assertEquals(cb, tracker.getOrCreate(key, now++));
    now += AppenderTracker.DEFAULT_TIMEOUT + 1000;
    tracker.clearStaleBuffers(now);
    assertEquals(0, tracker.keyList().size());
    assertEquals(0, tracker.bufferCount);
  }

  @Test
  public void destroy() {
    long now = 3000;
    CyclicBuffer<Object> cb = tracker.getOrCreate(key, now);
    cb.add(new Object());
    assertEquals(1, cb.length());
    tracker.removeBuffer(key);
    assertEquals(0, tracker.keyList().size());
    assertEquals(0, tracker.bufferCount);
    assertEquals(0, cb.length());
  }




}
