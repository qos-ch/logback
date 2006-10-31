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
import ch.qos.logback.classic.turbo.NOPClassicFilter;
import ch.qos.logback.core.appender.NOPAppender;

public class LoggerPerfTest extends TestCase {

  final static String KAL = "kal";
  String localhostName = null;
  
  public void setUp() throws Exception {
    localhostName = InetAddress.getLocalHost().getCanonicalHostName();
  }
  public void testSpeed() {
    long len = 1000*1000*10;
    loopBasic(len);
    double avg = loopBasic(len);
    
    System.out.println("Running on "+localhostName);
    // check for performance on KAL only
    if(KAL.equals(localhostName)) {
      assertTrue(30 > avg);
    }
    System.out.println("Average log time for disabled statements: "+avg+" nanos.");
  }
  
  public void testNOPFilterSpeed() {
    long len = 1000*1000*10;
    loopNopFilter(len);
    double avg = loopNopFilter(len);
    
    System.out.println("Running on "+localhostName);
    // check for performance on KAL only
    if(KAL.equals(localhostName)) {
      assertTrue(62 > avg);
    }
    System.out.println("Average log time for disabled statements: "+avg+" nanos.");
  }
  
  double loopBasic(long len) {
    LoggerContext lc = new LoggerContext();
    NOPAppender mopAppender = new NOPAppender();
    mopAppender.start();
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    long start = System.nanoTime();
    for(long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long end = System.nanoTime();
    return (end-start)/len;
  }
  
  double loopNopFilter(long len) {
    LoggerContext lc = new LoggerContext();
    NOPAppender mopAppender = new NOPAppender();
    NOPClassicFilter nopFilter = new NOPClassicFilter();
    nopFilter.setName("nop");
    mopAppender.start();
    lc.addFilter(nopFilter);
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    long start = System.nanoTime();
    for(long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long end = System.nanoTime();
    return (end-start)/len;
  }
  
}
