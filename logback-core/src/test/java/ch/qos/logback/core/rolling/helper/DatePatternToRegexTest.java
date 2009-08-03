/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling.helper;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.CoreConstants;

public class DatePatternToRegexTest {

  @Before
  public void setUp() throws Exception {
    // set(int year, int month, int date, int hourOfDay, int minute, int
    // second);
    // calendar.set(2009, 8, 3, 21, 57, 16);
  }

  @Test
  public void ISO8601() {
    SimpleDateFormat sdf = new SimpleDateFormat(CoreConstants.ISO8601_PATTERN);
    Calendar calendar = Calendar.getInstance();
    calendar.set(2009, 8, 3, 21, 57, 16);
    calendar.set(Calendar.MILLISECOND, 333);
    // 2009-09-03 21:57:16,333
    DateTokenConverter dtc = makeDTC(CoreConstants.ISO8601_PATTERN);
    verify(sdf, calendar, dtc);
  }

  void verify(SimpleDateFormat sdf, Calendar calendar, DateTokenConverter dtc) {
    String expected = sdf.format(calendar.getTime());
    String regex = dtc.toRegex();
    assertTrue("[" + expected + "] does not match regex [" + regex + "]",
        expected.matches(regex));
  }

  private DateTokenConverter makeDTC(String datePattern) {
    DateTokenConverter dtc = new DateTokenConverter();
    List<String> optionList = new ArrayList<String>();
    optionList.add(datePattern);
    dtc.setOptionList(optionList);
    dtc.start();
    return dtc;
  }
}
