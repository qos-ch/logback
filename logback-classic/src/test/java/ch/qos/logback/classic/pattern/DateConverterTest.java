/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2014, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.pattern;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

/**
 * Tests the {@link DateConverter}
 */
public class DateConverterTest {

  private final Calendar CAL = getCalendar(2014,1,1,14,28,30,456);
  private final String DATE_ISO801_UTC_STR  = "2014-01-01T14:28:30,456";
  private final String DATE_ISO801_AWST_STR = "2014-01-01T22:28:30,456"; // AWST = UTC+8:00
  private final long DATE_MS = CAL.getTimeInMillis();
  private ILoggingEvent EVENT = getLogEvent();

  @Test
  public void convertsCustomDateTimeFormat() {
    final String dateFormat = "(MM/dd/yy HH:mm)";
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    assertEquals(sdf.format(CAL.getTime()), getConverter(dateFormat).convert(EVENT));
  }

  @Test
  public void convertsIso8601WithUtcTzOption() {
    assertEquals(DATE_ISO801_UTC_STR, getConverter(CoreConstants.ISO8601_STR, "UTC").convert(EVENT));
  }

  @Test
  public void convertsIso8601WithTzOption() {
    assertEquals(DATE_ISO801_AWST_STR, getConverter(CoreConstants.ISO8601_STR, "Australia/Perth").convert(EVENT));
  }

  private ILoggingEvent getLogEvent() {
    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.getTimeStamp()).thenReturn(DATE_MS);
    return event;
  }

  private Calendar getCalendar(int year, int month, int dayOfMonth, int hour, int minute, int second, int millisecond) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    cal.set(Calendar.MONTH, month - 1);
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, second);
    cal.set(Calendar.MILLISECOND, millisecond);
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    return cal;
  }

  private DateConverter getConverter(String... options) {
    DateConverter converter = new DateConverter();
    converter.setOptionList(Arrays.asList(options));
    converter.start();
    return converter;
  }
}
