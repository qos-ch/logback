/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.sift

import ch.qos.logback.classic.gaffer.GafferConfigurator

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.testUtil.RandomUtil
import org.junit.Test
import ch.qos.logback.classic.ClassicTestConstants
import org.slf4j.MDC
import static junit.framework.Assert.assertNotNull
import ch.qos.logback.core.sift.AppenderTracker
import ch.qos.logback.core.read.ListAppender
import ch.qos.logback.core.util.StatusPrinter
import static junit.framework.Assert.assertEquals
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.status.StatusChecker
import ch.qos.logback.core.status.Status
import static junit.framework.Assert.assertNull
import org.junit.After
import static ch.qos.logback.classic.ClassicTestConstants.OUTPUT_DIR_PREFIX;

/**
 * @author Ceki G&uuml;c&uuml;
 */
class GSiftingAppenderTest {

  LoggerContext context = new LoggerContext();
  Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME)
  Logger logger = context.getLogger(this.getClass())
  int diff = RandomUtil.getPositiveInt();
  GafferConfigurator configurator = new GafferConfigurator(context);
  StatusChecker checker = new StatusChecker(context)

  @After
  public void tearDown() {
    MDC.clear();  
  }

  AppenderTracker execute(String path) {
    File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + path)
    String dslText = file.text
    configurator.run dslText

    GSiftingAppender sa = (GSiftingAppender) root.getAppender("SIFT");
    assertNotNull(sa)
    AppenderTracker tracker = sa.getAppenderTracker();
  }

  @Test
  void noDiscriminator() {
    AppenderTracker tracker = execute("sift/noDiscriminator.groovy")
    logger.debug("x")
    ListAppender unknownAppender = tracker.get("unknown", System.currentTimeMillis())
    assertNull(unknownAppender)
    checker.containsMatch(Status.ERROR, "Missing discriminator. Aborting")
  }

  @Test
  void sample0() {
    AppenderTracker tracker = execute("sift/sample0.groovy")
    logger.debug("x")
    ListAppender unknownAppender = tracker.get("unknown", System.currentTimeMillis())
    assertNotNull(unknownAppender)

    MDC.put("userid", "a");
    logger.debug("y");
    ListAppender aAppender = tracker.get("a", System.currentTimeMillis())
    assertNotNull(aAppender)

    assertEquals(1, unknownAppender.list.size);
    assertEquals("x", unknownAppender.list[0].message)
    assertEquals(1, aAppender.list.size);
    assertEquals("y", aAppender.list[0].message)
  }

  @Test
  void sample1() {
    AppenderTracker tracker = execute("sift/sample1.groovy")
    logger.debug("x")

    StatusPrinter.print context
    FileAppender unknownAppender = tracker.get("unknown", System.currentTimeMillis())
    assertNotNull(unknownAppender)
    assertEquals("FILE-unknown", unknownAppender.name)
    assertEquals(OUTPUT_DIR_PREFIX+"test-unknown.log", unknownAppender.file)

    MDC.put("userid", "a");
    logger.debug("y");
    FileAppender aAppender = tracker.get("a", System.currentTimeMillis())
    assertNotNull(aAppender)
    assertEquals("FILE-a", aAppender.name)
    assertEquals(OUTPUT_DIR_PREFIX+"test-a.log", aAppender.file)
    assertEquals("a - %msg%n", aAppender.encoder.pattern)
  }


}
