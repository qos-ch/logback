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
import ch.qos.logback.core.util.CoreTestConstants;

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

    File target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "test.log");
    target.mkdirs();
    target.delete();
  }

  @Test
  public void testRename() throws Exception {

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setLayout(layout);
    rfa.setContext(context);

    // rollover by the second
    String datePattern = "yyyy-MM-dd_HH_mm_ss";
    SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
    String[] filenames = new String[2];

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "test-%d{"
        + datePattern + "}");
    tbrp.setContext(context);
    tbrp.setParent(rfa);
    tbrp.start();

    rfa.setRollingPolicy(tbrp);
    rfa.start();


    Calendar cal0 = Calendar.getInstance();
    rfa.doAppend("Hello 0");
    
    DelayerUtil.delayUntilNextSecond(50);
    
    Calendar cal1 = Calendar.getInstance();
    rfa.doAppend("Hello 1");

    filenames[0] = CoreTestConstants.OUTPUT_DIR_PREFIX + "test-"
        + sdf.format(cal0.getTime());
    filenames[1] = CoreTestConstants.OUTPUT_DIR_PREFIX + "test-"
        + sdf.format(cal1.getTime());
    
  
    for (int i = 0; i < filenames.length; i++) {
      assertTrue(Compare.compare(filenames[i], CoreTestConstants.TEST_DIR_PREFIX
          + "witness/rolling/renaming." + i));
    }
  }
}
