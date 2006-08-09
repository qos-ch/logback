/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.rolling.helper.RollingCalendar;
import junit.framework.TestCase;

public class RollingCalendarTest extends TestCase {

  public RollingCalendarTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test() {
    {
      RollingCalendar rc = new RollingCalendar();
      assertEquals(RollingCalendar.TOP_OF_SECOND, rc
          .computeTriggeringPeriod("yyyy-MM-dd_HH_mm_ss"));
    }

    {
      RollingCalendar rc = new RollingCalendar();
      assertEquals(RollingCalendar.TOP_OF_MINUTE, rc
          .computeTriggeringPeriod("yyyy-MM-dd_HH_mm"));
    }
    
    {
      RollingCalendar rc = new RollingCalendar();
      assertEquals(RollingCalendar.TOP_OF_HOUR, rc
          .computeTriggeringPeriod("yyyy-MM-dd_HH"));
    }
    
    {
      RollingCalendar rc = new RollingCalendar();
      assertEquals(RollingCalendar.TOP_OF_DAY, rc
          .computeTriggeringPeriod("yyyy-MM-dd"));
    }
    
    {
      RollingCalendar rc = new RollingCalendar();
      assertEquals(RollingCalendar.TOP_OF_MONTH, rc
          .computeTriggeringPeriod("yyyy-MM"));
    }
    
  }

}
