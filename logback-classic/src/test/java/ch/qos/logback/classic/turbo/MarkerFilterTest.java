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
package ch.qos.logback.classic.turbo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public class MarkerFilterTest {

  static String MARKER_NAME = "toto";
  
  Marker totoMarker = MarkerFactory.getMarker(MARKER_NAME);
  LoggerContext context = new LoggerContext();
  Logger logger = context.getLogger(this.getClass());

  @Test
  public void testNoMarker() {
    MarkerFilter mkt = new MarkerFilter();
    mkt.start();
    assertFalse(mkt.isStarted());
    assertEquals(FilterReply.NEUTRAL, mkt.decide(newEvent(totoMarker)));
    assertEquals(FilterReply.NEUTRAL, mkt.decide(newEvent(null)));

  }


  @Test
  public void testBasic() {
    MarkerFilter mkt = new MarkerFilter();
    mkt.setMarker(MARKER_NAME);
    mkt.setOnMatch("ACCEPT");
    mkt.setOnMismatch("DENY");

    mkt.start();
    assertTrue(mkt.isStarted());
    assertEquals(FilterReply.DENY, mkt.decide(newEvent(null)));
    assertEquals(FilterReply.ACCEPT, mkt.decide(newEvent(totoMarker)));
  }
  
  LoggingEvent newEvent(Marker marker) {
    LoggingEvent event =  new LoggingEvent(null, logger, null, null, null, null);
    event.setMarker(marker);
    return event;
  }

  
}
