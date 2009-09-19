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
package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.appender.AbstractAppenderTest;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

public class RollingFileAppenderTest extends AbstractAppenderTest<Object> {

  RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
  Context context = new ContextBase();

  TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();

  @Before
  public void setUp() throws Exception {
    // noStartTest fails if the context is set in setUp
    // rfa.setContext(context);

    rfa.setLayout(new DummyLayout<Object>());
    rfa.setName("test");
    tbrp.setContext(context);
    tbrp.setParent(rfa);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Override
  protected Appender<Object> getAppender() {
    return rfa;
  }

  @Override
  protected Appender<Object> getConfiguredAppender() {
    rfa.setContext(context);
    tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX+"toto-%d.log");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);

    rfa.start();
    return rfa;
  }

  @Test
  public void testPrudentModeLogicalImplications() {
    rfa.setContext(context);
    // prudent mode will force "file" property to be null
    rfa.setFile("some non null value");
    rfa.setAppend(false);
    rfa.setImmediateFlush(false);
    rfa.setBufferedIO(true);
    rfa.setPrudent(true);

    tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX+"toto-%d.log");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);

    rfa.start();

    assertTrue(rfa.getImmediateFlush());
    assertTrue(rfa.isAppend());
    assertFalse(rfa.isBufferedIO());
    assertNull(rfa.rawFileProperty());
    assertTrue(rfa.isStarted());
  }

  @Test
  public void testPrudentModeLogicalImplicationsOnCompression() {
    rfa.setContext(context);
    rfa.setAppend(false);
    rfa.setImmediateFlush(false);
    rfa.setBufferedIO(true);
    rfa.setPrudent(true);

    tbrp.setFileNamePattern("toto-%d.log.zip");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);

    rfa.start();

    StatusManager sm = context.getStatusManager();
    assertFalse(rfa.isStarted());
    assertEquals(Status.ERROR, sm.getLevel());
  }

  @Test
  public void testFilePropertyAfterRollingPolicy() {
    rfa.setContext(context);
    rfa.setRollingPolicy(tbrp);
    rfa.setFile("x");
    StatusPrinter.print(context);
    StatusChecker statusChecker = new StatusChecker(context.getStatusManager());
    statusChecker.containsMatch(Status.ERROR,
        "File property must be set before any triggeringPolicy ");
  }

  @Test
  public void testFilePropertyAfterTriggeringPolicy() {
    rfa.setContext(context);
    rfa.setTriggeringPolicy(new SizeBasedTriggeringPolicy<Object>());
    rfa.setFile("x");
    StatusChecker statusChecker = new StatusChecker(context.getStatusManager());
    statusChecker.containsMatch(Status.ERROR,
        "File property must be set before any triggeringPolicy ");
  }
}
