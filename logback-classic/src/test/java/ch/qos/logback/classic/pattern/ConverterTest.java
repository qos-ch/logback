/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2005, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.net.SyslogConstants;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.FormatInfo;

public class ConverterTest extends TestCase {

  LoggerContext lc = new LoggerContext();
  Logger logger = lc.getLogger(ConverterTest.class);
  LoggingEvent le;
  List<String> optionList = new ArrayList<String>();

  public ConverterTest(String arg0) {
    super(arg0);

    Exception rootEx = getException("Innermost", null);
    Exception nestedEx = getException("Nested", rootEx);

    Exception ex = new Exception("Bogus exception", nestedEx);

    le = makeLoggingEvent(ex);
    // ex.printStackTrace();
  }

  // The LoggingEvent is massaged with an FCQN of FormattingConverter. This
  // forces the
  // returned caller information to match the caller stack for this this
  // particular test.
  LoggingEvent makeLoggingEvent(Exception ex) {
    return new LoggingEvent(
        ch.qos.logback.core.pattern.FormattingConverter.class.getName(),
        logger, Level.INFO, "Some message", ex, null);
  }

  Exception getException(String msg, Exception cause) {
    return new Exception(msg, cause);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testLineOfCaller() {
    {
      DynamicConverter<LoggingEvent> converter = new LineOfCallerConverter();
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      // the number below should be the line number of the previous line
      assertEquals("74", buf.toString());
    }
  }

  public void testLevel() {
    {
      DynamicConverter<LoggingEvent> converter = new LevelConverter();
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      assertEquals("INFO", buf.toString());
    }
    {
      DynamicConverter<LoggingEvent> converter = new LevelConverter();
      converter.setFormattingInfo(new FormatInfo(1, 1, true, false));
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      assertEquals("I", buf.toString());
    }
  }

  public void testThread() {
    DynamicConverter<LoggingEvent> converter = new ThreadConverter();
    StringBuffer buf = new StringBuffer();
    converter.write(buf, le);
    assertEquals("main", buf.toString());
  }

  public void testMessage() {
    DynamicConverter<LoggingEvent> converter = new MessageConverter();
    StringBuffer buf = new StringBuffer();
    converter.write(buf, le);
    assertEquals("Some message", buf.toString());
  }

  public void testLineSeparator() {
    DynamicConverter<LoggingEvent> converter = new LineSeparatorConverter();
    StringBuffer buf = new StringBuffer();
    converter.write(buf, le);
    assertEquals(CoreGlobal.LINE_SEPARATOR, buf.toString());
  }

  public void testException() {
    {
      DynamicConverter<LoggingEvent> converter = new ThrowableInformationConverter();
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      // System.out.println(buf);
    }

    {
      DynamicConverter<LoggingEvent> converter = new ThrowableInformationConverter();
      this.optionList.add("3");
      converter.setOptionList(this.optionList);
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      // System.out.println(buf);
    }
  }

  public void testLogger() {
    {
      DynamicConverter<LoggingEvent> converter = new LoggerConverter();
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      assertEquals(this.getClass().getName(), buf.toString());
    }

    {
      DynamicConverter<LoggingEvent> converter = new LoggerConverter();
      this.optionList.add("20");
      converter.setOptionList(this.optionList);
      converter.start();
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      assertEquals("c.q.l.c.p.ConverterTest", buf.toString());
    }
  }

  public void testClass() {
    {
      DynamicConverter<LoggingEvent> converter = new ClassOfCallerConverter();
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      assertEquals(this.getClass().getName(), buf.toString());
    }
  }

  public void testMethodOfCaller() {
    {
      DynamicConverter<LoggingEvent> converter = new MethodOfCallerConverter();
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      assertEquals("testMethodOfCaller", buf.toString());
    }
  }

  public void testFileOfCaller() {
    {
      DynamicConverter<LoggingEvent> converter = new FileOfCallerConverter();
      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      assertEquals("ConverterTest.java", buf.toString());
    }
  }

  public void testCallerData() {
    {
      DynamicConverter<LoggingEvent> converter = new CallerDataConverter();
      converter.start();

      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      if (buf.length() < 10) {
        fail("buf is too short");
      }
    }

    {
      DynamicConverter<LoggingEvent> converter = new CallerDataConverter();
      this.optionList.add("2");
      this.optionList.add("XXX");
      converter.setOptionList(this.optionList);
      converter.start();

      StringBuffer buf = new StringBuffer();
      LoggingEvent event = makeLoggingEvent(null);
      event.setMarker(MarkerFactory.getMarker("XXX"));
      converter.write(buf, event);
      if (buf.length() < 10) {
        fail("buf is too short");
      }
    }

    {
      DynamicConverter<LoggingEvent> converter = new CallerDataConverter();
      this.optionList.clear();
      this.optionList.add("2");
      this.optionList.add("XXX");
      this.optionList.add("*");
      converter.setOptionList(this.optionList);
      converter.start();

      StringBuffer buf = new StringBuffer();
      LoggingEvent event = makeLoggingEvent(null);
      event.setMarker(MarkerFactory.getMarker("YYY"));
      converter.write(buf, event);
      if (buf.length() < 10) {
        fail("buf is too short");
      }
    }
    {
      DynamicConverter<LoggingEvent> converter = new CallerDataConverter();
      this.optionList.clear();
      this.optionList.add("2");
      this.optionList.add("XXX");
      this.optionList.add("+");
      converter.setOptionList(this.optionList);
      converter.start();

      StringBuffer buf = new StringBuffer();
      LoggingEvent event = makeLoggingEvent(null);
      event.setMarker(MarkerFactory.getMarker("YYY"));
      converter.write(buf, event);
      if (buf.length() < 10) {
        fail("buf is too short");
      }
    }

    {
      DynamicConverter<LoggingEvent> converter = new CallerDataConverter();
      this.optionList.clear();
      this.optionList.add("2");
      this.optionList.add("XXX");
      this.optionList.add("*");
      converter.setOptionList(this.optionList);
      converter.start();

      StringBuffer buf = new StringBuffer();
      converter.write(buf, le);
      if (buf.length() < 10) {
        fail("buf is too short");
      }
      // System.out.println(buf);
    }

  }

  public void testRelativeTime() throws Exception {
    {
      DynamicConverter<LoggingEvent> converter = new RelativeTimeConverter();
      StringBuffer buf0 = new StringBuffer();
      converter.write(buf0, makeLoggingEvent(null));
      StringBuffer buf1 = new StringBuffer();
      converter.write(buf1, makeLoggingEvent(null));
      assertEquals(buf0.toString(), buf1.toString());
      int rt0 = Integer.parseInt(buf0.toString());
      if (rt0 < 50) {
        fail("relative time should be > 50, but it is " + rt0);
      }
    }
  }

  public void testSyslogStart() throws Exception {
    {
      DynamicConverter<LoggingEvent> converter = new SyslogStartConverter();
      this.optionList.clear();
      this.optionList.add("MAIL");
      converter.setOptionList(this.optionList);
      converter.start();

      LoggingEvent event = makeLoggingEvent(null);

      StringBuffer buf = new StringBuffer();
      converter.write(buf, event);

      String expected = "<"
          + (SyslogConstants.LOG_MAIL + SyslogConstants.INFO_SEVERITY) + ">";
      assertTrue(buf.toString().startsWith(expected));
    }
  }

  public void testMDCConverter() throws Exception {
    MDC.clear();
    MDC.put("someKey", "someValue");
    MDCConverter converter = new MDCConverter();
    this.optionList.clear();
    this.optionList.add("someKey");
    converter.setOptionList(optionList);
    converter.start();

    LoggingEvent event = makeLoggingEvent(null);

    String result = converter.convert(event);
    assertEquals("someValue", result);
  }

}
