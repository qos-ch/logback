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
package ch.qos.logback.classic.json;

import junit.framework.TestCase;
import ch.qos.logback.classic.net.SerializationPerfTest;
import ch.qos.logback.classic.net.testObjectBuilders.Builder;
import ch.qos.logback.classic.net.testObjectBuilders.TrivialLoggingEventVOBuilder;
import ch.qos.logback.classic.spi.LoggingEventVO;

/**
 * Based on {@link SerializationPerfTest}.
 * @author Pierre Queinnec
 */
public class JsonPerfTest extends TestCase {

  private final static int loopNumber = 10000;
  private final static int pauseFrequency = 10;
  private final static long pauseLengthInMillis = 20;

  private JsonLayout jsonLayout;

  @Override
  protected void setUp() throws Exception {
    this.jsonLayout = new JsonLayout();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    this.jsonLayout = null;
    super.tearDown();
  }

  public void runPerfTest(Builder builder, String label) throws Exception {
    // long time1 = System.nanoTime();

    // first run for just in time compiler
    int pauseCounter = 0;
    for (int i = 0; i < loopNumber; i++) {
      LoggingEventVO event = (LoggingEventVO) builder.build(i);
      jsonLayout.doLayout(event);

      if (++pauseCounter >= pauseFrequency) {
        Thread.sleep(pauseLengthInMillis);
        pauseCounter = 0;
      }
    }

    // second run
    Long t1;
    Long t2;
    Long total = 0L;
    pauseCounter = 0;
    // System.out.println("Beginning mesured run");
    for (int i = 0; i < loopNumber; i++) {
      t1 = System.nanoTime();

      LoggingEventVO event = (LoggingEventVO) builder.build(i);
      jsonLayout.doLayout(event);

      t2 = System.nanoTime();
      total += (t2 - t1);

      if (++pauseCounter >= pauseFrequency) {
        Thread.sleep(pauseLengthInMillis);
        pauseCounter = 0;
      }
    }
    total /= 1000;
    System.out.println(label + " : average time = " + total / loopNumber
        + " microsecs after " + loopNumber + " writes.");

    // long time2 = System.nanoTime();
    // System.out.println("********* -> Time needed to run the test method: " +
    // Long.toString(time2-time1));
  }

  public void testWithJson() throws Exception {
    Builder builder = new TrivialLoggingEventVOBuilder();
    runPerfTest(builder, "LoggingEventVO JSON");
  }

}
