/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.StatusPrinter;


/**
 * 
 * This test case aims to unit test/reproduce problems encountered while
 * renaming the log file under windows.
 * 
 * @author Ceki
 *
 */
public class RenamingTest extends TestCase {
  
  Layout layout;
  Context context = new ContextBase();
  
  public RenamingTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
    layout = new EchoLayout();
    
	File target = new File("src/test/output/test.log");
	target.mkdirs();
	target.delete();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testRename() throws Exception {
  
    
    RollingFileAppender rfa = new RollingFileAppender();
    rfa.setLayout(layout);
    rfa.setContext(context);
    
    // rollover by the second
    String datePattern = "yyyy-MM-dd_HH_mm_ss";
    SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
    String[] filenames = new String[2];

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setFileNamePattern("src/test/output/test-%d{" + datePattern + "}");
    tbrp.setActiveFileName("src/test/output/test.log");
    tbrp.setContext(context);
    tbrp.start();
    
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    StatusPrinter.print(context.getStatusManager());
    Calendar cal = Calendar.getInstance();

    rfa.doAppend("Hello 0");
    Thread.sleep(5000);
    rfa.doAppend("Hello 1");
    
    filenames[0] = "src/test/output/test-" + sdf.format(cal.getTime());
    filenames[1] = "src/test/output/test.log";

    for (int i = 0; i < filenames.length; i++) {
      System.out.println("before i="+i);
      assertTrue(Compare.compare(filenames[i], "src/test/witness/rolling/renaming." + i));
      System.out.println("post i="+i);
    }
  }
}
