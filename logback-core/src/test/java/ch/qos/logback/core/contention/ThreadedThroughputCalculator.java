/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.contention;


/**
 * Useful scaffolding to measure the throughput of certain operations when
 * invoked by multiple threads.
 * 
 * @author Joern Huxhorn
 * @author Ralph Goers
 * @author Ceki Gulcu
 */
public class ThreadedThroughputCalculator extends MultiThreadedHarness {


  public ThreadedThroughputCalculator(long overallDurationInMillis) {
    super(overallDurationInMillis);
  }

  public void printThroughput(String msg) throws InterruptedException {
    printThroughput(msg, false);
  }
  
  public void printThroughput(String msg, boolean detailed) throws InterruptedException {
    long sum = 0;
    for (RunnableWithCounterAndDone r : runnableArray) {
      if(detailed) {
        System.out.println(r +" count="+r.getCounter());
      }
      sum += r.getCounter();
    }
    
    System.out.println(msg + "total of " + sum + " operations, or "
        + ((sum) / overallDurationInMillis) + " operations per millisecond");
  }
}
