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
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.appender.AbstractAppenderTest;
import ch.qos.logback.core.encoder.DummyEncoder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RollingFileAppenderTest extends AbstractAppenderTest<Object> {

  RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
  Context context = new ContextBase();

  TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();
  int diff = RandomUtil.getPositiveInt();
  String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

  @Before
  public void setUp() throws Exception {
    // noStartTest fails if the context is set in setUp
    // rfa.setContext(context);

    rfa.setEncoder(new DummyEncoder<Object>());
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
    tbrp
            .setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log");
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
    rfa.setPrudent(true);

    tbrp
            .setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);

    rfa.start();

    assertTrue(rfa.isAppend());
    assertNull(rfa.rawFileProperty());
    assertTrue(rfa.isStarted());
  }

  @Test
  public void testPrudentModeLogicalImplicationsOnCompression() {
    rfa.setContext(context);
    rfa.setAppend(false);
    rfa.setPrudent(true);

    tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log.zip");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);

    rfa.start();

    StatusChecker checker = new StatusChecker(context);
    assertFalse(rfa.isStarted());
    assertEquals(Status.ERROR, checker.getHighestLevel(0));
  }

  @Test
  public void testFilePropertyAfterRollingPolicy() {
    rfa.setContext(context);
    rfa.setRollingPolicy(tbrp);
    rfa.setFile("x");
    StatusPrinter.print(context);
    StatusChecker statusChecker = new StatusChecker(context.getStatusManager());
    statusChecker.assertContainsMatch(Status.ERROR,
            "File property must be set before any triggeringPolicy ");
  }

  @Test
  public void testFilePropertyAfterTriggeringPolicy() {
    rfa.setContext(context);
    rfa.setTriggeringPolicy(new SizeBasedTriggeringPolicy<Object>());
    rfa.setFile("x");
    StatusChecker statusChecker = new StatusChecker(context.getStatusManager());
    statusChecker.assertContainsMatch(Status.ERROR,
            "File property must be set before any triggeringPolicy ");
  }

  @Test
  public void testFileNameWithParenthesis() {
    // if ')' is not escaped, the test throws
    // java.lang.IllegalStateException: FileNamePattern [.../program(x86)/toto-%d.log] does not contain a valid DateToken
    rfa.setContext(context);
    tbrp
            .setFileNamePattern(randomOutputDir + "program(x86)/toto-%d.log");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();
    rfa.doAppend("hello");
  }

  @Test
  public void stopTimeBasedRollingPolicy() {
    rfa.setContext(context);

    tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log.zip");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    StatusPrinter.print(context);
    assertTrue(tbrp.isStarted());
    assertTrue(rfa.isStarted());
    rfa.stop();
    assertFalse(rfa.isStarted());
    assertFalse(tbrp.isStarted());

  }

  @Test
  public void stopFixedWindowRollingPolicy() {
    rfa.setContext(context);
    rfa.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-.log");

    FixedWindowRollingPolicy fwRollingPolicy = new FixedWindowRollingPolicy();
    fwRollingPolicy.setContext(context);
    fwRollingPolicy.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%i.log.zip");
    fwRollingPolicy.setParent(rfa);
    fwRollingPolicy.start();
    SizeBasedTriggeringPolicy sbTriggeringPolicy = new SizeBasedTriggeringPolicy();
    sbTriggeringPolicy.setContext(context);
    sbTriggeringPolicy.start();

    rfa.setRollingPolicy(fwRollingPolicy);
    rfa.setTriggeringPolicy(sbTriggeringPolicy);

    rfa.start();

    StatusPrinter.print(context);
    assertTrue(fwRollingPolicy.isStarted());
    assertTrue(sbTriggeringPolicy.isStarted());
    assertTrue(rfa.isStarted());
    rfa.stop();
    assertFalse(rfa.isStarted());
    assertFalse(fwRollingPolicy.isStarted());
    assertFalse(sbTriggeringPolicy.isStarted());

  }

  /**
   * Test for http://jira.qos.ch/browse/LOGBACK-796
   */
  @Test
  public void testFileShouldNotMatchFileNamePattern() {
    rfa.setContext(context);
    rfa.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX + "x-2013-04.log");
    tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "x-%d{yyyy-MM}.log");
    tbrp.start();

    rfa.setRollingPolicy(tbrp);
    rfa.start();
    StatusChecker statusChecker = new StatusChecker(context);
    final String msg = "File property collides with fileNamePattern. Aborting.";
    boolean containsMatch = statusChecker.containsMatch(Status.ERROR, msg);
    assertTrue("Missing error: " + msg, containsMatch);
  }

}
