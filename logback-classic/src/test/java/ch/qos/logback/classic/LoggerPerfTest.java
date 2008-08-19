/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;


import java.net.InetAddress;

import junit.framework.TestCase;

import org.slf4j.helpers.BogoPerf;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.core.appender.NOPAppender;

public class LoggerPerfTest extends TestCase {

  final static String KAL = "kal";
  String localhostName = null;
  static long NORMAL_RUN_LENGTH = 1000*1000;

  static long REFERENCE_BIPS = 9324;
  
  public void setUp() throws Exception {
    localhostName = InetAddress.getLocalHost().getCanonicalHostName();
  }
  public void testBasic() {
    basicDurationInNanos(NORMAL_RUN_LENGTH);
    double avg = basicDurationInNanos(NORMAL_RUN_LENGTH); 
    
    long referencePerf = 17;
    BogoPerf.assertDuration(avg, referencePerf, REFERENCE_BIPS);
    //System.out.println("Average log time for disabled statements: "+avg+" nanos.");
  }
  
  public void testParameterized() {
    loopParameterized(NORMAL_RUN_LENGTH);
    double avgDuration = loopParameterized(NORMAL_RUN_LENGTH); 
    long referencePerf = 36;
    BogoPerf.assertDuration(avgDuration, referencePerf, REFERENCE_BIPS);
    //System.out.println("Average log time for disabled (parameterized) statements: "+avg+" nanos.");
  }
  
  
  public void testNOPFilter() {
    loopNopFilter(NORMAL_RUN_LENGTH);
    double avg = loopNopFilter(NORMAL_RUN_LENGTH);
    //System.out.println("Average log time for disabled (NOPFilter) statements: "+avg+" nanos.");
    long referencePerf = 48;
    BogoPerf.assertDuration(avg, referencePerf, REFERENCE_BIPS);
  }
  
  double basicDurationInNanos(long len) {
    LoggerContext lc = new LoggerContext();
    NOPAppender<LoggingEvent> mopAppender = new NOPAppender<LoggingEvent>();
    mopAppender.start();
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    for(long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long start = System.nanoTime();
    for(long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long end = System.nanoTime();
    return (end-start)/len;
  }

  double loopParameterized(long len) {
    LoggerContext lc = new LoggerContext();
    NOPAppender<LoggingEvent> mopAppender = new NOPAppender<LoggingEvent>();
    mopAppender.start();
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    for(long i = 0; i < len; i++) {
      logger.debug("Toto {}", i);
    }
    long start = System.nanoTime();
    for(long i = 0; i < len; i++) {
      logger.debug("Toto {}", i);
    }
    long end = System.nanoTime();
    return (end-start)/len;
  }
  
  double loopNopFilter(long len) {
    LoggerContext lc = new LoggerContext();
    NOPAppender<LoggingEvent> mopAppender = new NOPAppender<LoggingEvent>();
    NOPTurboFilter nopFilter = new NOPTurboFilter();
    nopFilter.setName("nop");
    mopAppender.start();
    lc.addTurboFilter(nopFilter);
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    for(long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long start = System.nanoTime();
    for(long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long end = System.nanoTime();
    return (end-start)/len;
  }
  
}
