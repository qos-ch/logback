/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.ConverterTest;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.pattern.parser.AbstractPatternLayoutBaseTest;


public class PatternLayoutTest extends AbstractPatternLayoutBaseTest {

  Logger logger = LoggerFactory.getLogger(ConverterTest.class);
  LoggingEvent le;
  List optionList = new ArrayList();

  public PatternLayoutTest(String arg0) {
    super(arg0);

    Exception ex = new Exception("Bogus exception");

    le = makeLoggingEvent(ex);
    // ex.printStackTrace();
  }
  
  LoggingEvent makeLoggingEvent(Exception ex) {
    return new LoggingEvent(ch.qos.logback.core.pattern.FormattingConverter.class
        .getName(), logger, Level.INFO, "Some message", ex);
  }

  public Object getEventObject() {
    return makeLoggingEvent(null);
  }
  
  public PatternLayoutBase getPatternLayoutBase() {
    return new PatternLayout();
  }

  public void testOK() {
    PatternLayout pl = new PatternLayout();
    pl.setPattern("%d %le [%t] %lo{30} - %m%n");
    Context context = new LoggerContext();
    pl.setContext(context);
    pl.start();
    String val = pl.doLayout(getEventObject());
    // 2006-02-01 22:38:06,212 INFO [main] c.q.l.pattern.ConverterTest - Some message
    String regex = Contants4T.ISO_REGEX+" INFO \\[main] c.q.l.c.pattern.ConverterTest - Some message\\s*";
    assertTrue(val.matches(regex));
  }

  public void testNoExeptionHandler() {
    PatternLayout pl = new PatternLayout();
    pl.setPattern("%m%n");
    Context context = new LoggerContext();
    pl.setContext(context);
    pl.start();
    String val = pl.doLayout(le);
    assertTrue(val.contains("java.lang.Exception: Bogus exception"));
  }

  public void testNopExeptionHandler() {
    PatternLayout pl = new PatternLayout();
    pl.setPattern("%nopex %m%n");
    Context context = new LoggerContext();
    pl.setContext(context);
    pl.start();
    String val = pl.doLayout(le);
    assertTrue(!val.contains("java.lang.Exception: Bogus exception"));
  }
  
  public void testWithLettersComingFromLog4j() {
    PatternLayout pl = new PatternLayout();
    //Letters: p = level and c = logger
    pl.setPattern("%d %p [%t] %c{30} - %m%n");
    Context context = new LoggerContext();
    pl.setContext(context);
    pl.start();
    String val = pl.doLayout(getEventObject());
    // 2006-02-01 22:38:06,212 INFO [main] c.q.l.pattern.ConverterTest - Some message
    String regex = Contants4T.ISO_REGEX+" INFO \\[main] c.q.l.c.pattern.ConverterTest - Some message\\s*";
    assertTrue(val.matches(regex));
  }
  
  static public Test suite() {
    TestSuite suite = new TestSuite();
    //suite.addTest(new PatternLayoutTest("testNopExeptionHandler"));
    suite.addTestSuite(PatternLayoutTest.class);
    return suite;
  }
}
