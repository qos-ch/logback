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

import org.junit.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public class DuplicateMessageFilterTest {

  LoggerContext context = new LoggerContext();
  Logger logger = context.getLogger(this.getClass());
  
  @Test
  public void smoke() {
    DuplicateMessageFilter dmf = new DuplicateMessageFilter();
    dmf.setAllowedRepetitions(0);
    dmf.start();
    assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent("x")));
    assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent("y")));
    assertEquals(FilterReply.DENY, dmf
        .decide(newEvent("x")));
    assertEquals(FilterReply.DENY, dmf
        .decide(newEvent("y")));
  }

  @Test
  public void memoryLoss() {
    DuplicateMessageFilter dmf = new DuplicateMessageFilter();
    dmf.setAllowedRepetitions(1);
    dmf.setCacheSize(1);
    dmf.start();
    assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent("a")));
    assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent("b")));
    assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent("a")));
  }

  @Test
  public void many() {
    DuplicateMessageFilter dmf = new DuplicateMessageFilter();
    dmf.setAllowedRepetitions(0);
    int cacheSize = 10;
    int margin = 2;
    dmf.setCacheSize(cacheSize);
    dmf.start();
    for (int i = 0; i < cacheSize + margin; i++) {
      assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent("a" + i)));
    }
    for (int i = cacheSize - 1; i >= margin; i--) {
      assertEquals(FilterReply.DENY, dmf.decide(newEvent("a" + i)));
    }
    for (int i = margin - 1; i >= 0; i--) {
      assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent("a" + i)));
    }
  }

  @Test
  // isXXXEnabled invokes decide with a null format
  // http://jira.qos.ch/browse/LBCLASSIC-134
  public void nullFormat() {
    DuplicateMessageFilter dmf = new DuplicateMessageFilter();
    dmf.setAllowedRepetitions(0);
    dmf.setCacheSize(10);
    dmf.start();
    assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent(null)));
    assertEquals(FilterReply.NEUTRAL, dmf.decide(newEvent(null)));
  }

  LoggingEvent newEvent(String m) {
    return new LoggingEvent(null, logger, null, m, null, null);
  }

}
