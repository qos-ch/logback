/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.util;

import junit.framework.TestCase;

public class DurationTest extends TestCase {

  static long HOURS_CO = 60*60;
  static long DAYS_CO = 24*60*60;
  
  public DurationTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test() {
    {
      Duration d = Duration.valueOf("12");
      assertEquals(12, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("8 milliseconds");
      assertEquals(8, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("10.7 millisecond");
      assertEquals(10, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("10 SECOnds");
      assertEquals(10 * 1000, d.getMilliseconds());
    }

    {
      Duration d = Duration.valueOf("12seconde");
      assertEquals(12 * 1000, d.getMilliseconds());
    }

    {
      Duration d = Duration.valueOf("14 SECONDES");
      assertEquals(14 * 1000, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("12second");
      assertEquals(12 * 1000, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("10.7 seconds");
      assertEquals(10700, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("1 minute");
      assertEquals(1000*60, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("2.2 minutes");
      assertEquals(2200*60, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("1 hour");
      assertEquals(1000*HOURS_CO, d.getMilliseconds());
    }
    
    {
      Duration d = Duration.valueOf("4.2 hours");
      assertEquals(4200*HOURS_CO, d.getMilliseconds());
    }

    {
      Duration d = Duration.valueOf("5 days");
      assertEquals(5000*DAYS_CO, d.getMilliseconds());
    }
  }
}
