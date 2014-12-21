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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class TimeBasedFileNamingAndTriggeringPolicyBaseTest {

  static long MILLIS_IN_MINUTE = 60*1000;

  Context context = new ContextBase();
  RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();
  DefaultTimeBasedFileNamingAndTriggeringPolicy<Object> timeBasedFNATP = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();

  @Before
  public void setUp() {
    rfa.setContext(context);
    tbrp.setContext(context);
    timeBasedFNATP.setContext(context);

    rfa.setRollingPolicy(tbrp);
    tbrp.setParent(rfa);
    tbrp.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedFNATP);
    timeBasedFNATP.setTimeBasedRollingPolicy(tbrp);
  }

  @Test
  public void singleDate() {
    // Tuesday December 20th 17:59:01 CET 2011
    long startTime = 1324400341553L;
    tbrp.setFileNamePattern("foo-%d{yyyy-MM'T'mm}.log");
    tbrp.start();

    timeBasedFNATP.setCurrentTime(startTime);
    timeBasedFNATP.start();

    timeBasedFNATP.setCurrentTime(startTime+MILLIS_IN_MINUTE);
    timeBasedFNATP.isTriggeringEvent(null, null);
    String elapsedPeriodsFileName = timeBasedFNATP.getElapsedPeriodsFileName();
    assertEquals("foo-2011-12T59.log", elapsedPeriodsFileName);
  }

  // see "log rollover should be configurable using %d multiple times in file name pattern"
  // http://jira.qos.ch/browse/LBCORE-242

  @Test
  public void multiDate() {
    // Tuesday December 20th 17:59:01 CET 2011
    long startTime = 1324400341553L;
    tbrp.setFileNamePattern("foo-%d{yyyy-MM, AUX}/%d{mm}.log");
    tbrp.start();

    timeBasedFNATP.setCurrentTime(startTime);
    timeBasedFNATP.start();

    timeBasedFNATP.setCurrentTime(startTime+MILLIS_IN_MINUTE);
    timeBasedFNATP.isTriggeringEvent(null, null);
    String elapsedPeriodsFileName = timeBasedFNATP.getElapsedPeriodsFileName();
    assertEquals("foo-2011-12/59.log", elapsedPeriodsFileName);
  }

}
