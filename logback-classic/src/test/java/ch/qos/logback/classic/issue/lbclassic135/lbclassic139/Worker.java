/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.issue.lbclassic135.lbclassic139;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

/**
 * 
 * @author Olivier Cailloux
 * 
 */
public class Worker extends RunnableWithCounterAndDone {
  static final int SLEEP_DUIRATION = 50;
  
  private Logger logger; 
  private final Object lock = new Object();

  final LoggerContext loggerContext;
  Worker(LoggerContext lc) {
    loggerContext = lc;
    logger = lc.getLogger(this.getClass());
  }
  
  public void run() {
    print("entered run()");
    while (!isDone()) {
      synchronized (lock) {
        sleep();
        logger.info("lock the logger");
      }
    }
    print("leaving run()");
  }

  @Override
  public String toString() {
    print("In Worker.toString() - about to access lock");
    synchronized (lock) {
      print("In Worker.toString() - got the lock");
      //sleep();
      final StringBuffer buf = new StringBuffer("STATUS");
      return buf.toString();
    }
  }
  
  public void sleep() {
    try {
      print("About to go to sleep");
      Thread.sleep(SLEEP_DUIRATION);
      print("just woke up");
    } catch (InterruptedException exc) {
      exc.printStackTrace();
    }
  }
  
  void print(String msg) {
    String thread = Thread.currentThread().getName();
    System.out.println("["+thread+"] "+msg);
  }
}