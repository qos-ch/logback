/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.Constants;

/**
 * 
 * This test case aims to unit test/reproduce problems encountered while
 * renaming the log file under windows.
 * 
 * @author Ceki
 * 
 */
public class RenamingTest {

  Layout<Object> layout;
  Context context = new ContextBase();

  @Before
  public void setUp() throws Exception {
    layout = new EchoLayout<Object>();

    File target = new File(Constants.OUTPUT_DIR_PREFIX + "test.log");
    target.mkdirs();
    target.delete();
  }

  @Test
  public void testRename() throws Exception {

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setLayout(layout);
    rfa.setContext(context);
    rfa.setFile(Constants.OUTPUT_DIR_PREFIX + "test.log");

    // rollover by the second
    String datePattern = "yyyy-MM-dd_HH_mm_ss";
    SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
    String[] filenames = new String[2];

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setFileNamePattern(Constants.OUTPUT_DIR_PREFIX + "test-%d{"
        + datePattern + "}");
    // tbrp.setActiveFileName("src/test/output/test.log");
    tbrp.setContext(context);
    tbrp.setParent(rfa);
    tbrp.start();

    rfa.setRollingPolicy(tbrp);
    rfa.start();

    // StatusPrinter.print(context.getStatusManager());
    Calendar cal = Calendar.getInstance();

    rfa.doAppend("Hello 0");
    DelayerUtil.delayUntilNextSecond(50);
    rfa.doAppend("Hello 1");

    filenames[0] = Constants.OUTPUT_DIR_PREFIX + "test-"
        + sdf.format(cal.getTime());
    filenames[1] = Constants.OUTPUT_DIR_PREFIX + "test.log";

    for (int i = 0; i < filenames.length; i++) {
      // System.out.println("before i=" + i);
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/renaming." + i));
      // System.out.println("post i=" + i);
    }
  }
}
