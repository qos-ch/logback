/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.multiJVM;

import org.slf4j.Logger;

public class LoggingThread extends Thread {
  static String msgLong = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

  final long len;
  final Logger logger;
  private double durationPerLog;

  public LoggingThread(Logger logger, long len) {
    this.logger = logger;
    this.len = len;
  }

  public void run() {
    long before = System.nanoTime();
    for (int i = 0; i < len; i++) {
      logger.debug(msgLong + " " + i);
//      try {
//        Thread.sleep(100);
//      } catch (InterruptedException e) {
//      }
    }
    // in microseconds
    durationPerLog = (System.nanoTime() - before) / (len * 1000.0);
  }

  public double getDurationPerLogInMicroseconds() {
    return durationPerLog;
  }
  
  
}
