/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter4;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.layout.EchoLayout;



public class IO extends Thread {
  static String msgLong = "ABCDEGHIJKLMNOPQRSTUVWXYZabcdeghijklmnopqrstuvwxyz1234567890";
  static String msgShort = "Hello";
  static boolean scarceCPU;
  static int numThreads;
  static long l;
  static boolean longMessage;
  long len;
  boolean buffered;
  boolean immediateFlush;
  Logger logger;
  LoggerContext context;
  double throughput;

  public IO(boolean _buffered, boolean _immediateFlush, long _len) {
    this.len = _len;
    this.buffered = _buffered;
    this.immediateFlush = _immediateFlush;
    context = new LoggerContext();
    logger = context.getLogger("logger-" + getName());

    // A FileAppender is created according to the buffering and
    // immediate flush setting of this IO instance.
    FileAppender fa = new FileAppender();

    if (longMessage) {
      PatternLayout pa = new PatternLayout();
      pa.setPattern("%r %5p %c [%t] - %m%n");
      fa.setLayout(pa);
    } else {
      fa.setLayout(new EchoLayout());
    }

    fa.setFile(getName() + ".log");
    fa.setAppend(false);
    fa.setImmediateFlush(immediateFlush);
    fa.setBufferedIO(buffered);
    fa.setContext(context);
    fa.start();
    
  }

  public static void main(String[] argv) throws Exception {
    if (argv.length != 4) {
      usage("Wrong number of arguments.");
    }

    l = Integer.parseInt(argv[0]);
    numThreads = Integer.parseInt(argv[1]);
    scarceCPU = "true".equalsIgnoreCase(argv[2]);
    longMessage = "long".equalsIgnoreCase(argv[3]);

    // ----------------------------------------------------
    // first test with unbuffered IO and immediate flushing
    perfCase(false, true, l);

    // ----------------------------------------------------
    // Second test with unbuffered IO and no immediate flushing
    perfCase(false, false, l);

    // ----------------------------------------------------
    // Third test with buffered IO and no immediate flushing
    perfCase(true, false, l);

    // There is no fourth test as buffered IO and immediate flushing
    // do not make sense.
  }

  static void usage(String msg) {
    System.err.println(msg);
    System.err.println("Usage: java " + IO.class.getName() +
      " runLength numThreads scarceCPU (short|long)\n" +
      "   runLength (integer) the number of logs to generate perthread\n" +
      "   numThreads (integer) the number of threads.\n" +
      "   scarceCPU (boolean) if true an additional CPU intensive thread is created\n" +
      "   (short|long) length of log messages.");
    System.exit(1);
  }

  static void perfCase(boolean buffered, boolean immediateFlush, long len)
    throws Exception {
    IO[] threads = new IO[numThreads];
    Counter counterThread = null;

    if (scarceCPU) {
      counterThread = new Counter();
      counterThread.start();
    }

    // First create the threads 
    for (int i = 0; i < numThreads; i++) {
      threads[i] = new IO(buffered, immediateFlush, len);
    }

    // then start them
    for (int i = 0; i < numThreads; i++) {
      threads[i].start();
    }

    // wait for them to stop, compute the average throughputs
    double sum = 0;

    for (int i = 0; i < numThreads; i++) {
      threads[i].join();
      sum += threads[i].throughput;
    }

    if (scarceCPU) {
      // setting the interrupted field will cause counterThread to stop
      counterThread.interrupted = true;
      counterThread.join();
    }

    System.out.println("On average throughput of " + (sum / numThreads) +
      " logs per millisecond.");
    System.out.println("------------------------------------------------");
  }

  public void run() {
    String msg = msgShort;

    if (longMessage) {
      msg = msgLong;
    }

    long before = System.currentTimeMillis();

    for (int i = 0; i < len; i++) {
      logger.debug(msg);
    }

    throughput = (len * 1.0) / (System.currentTimeMillis() - before);
    System.out.println(getName() + ", buffered: " + buffered +
      ", immediateFlush: " + immediateFlush + ", throughput: " + throughput +
      " logs per millisecond.");
  }
}


class Counter extends Thread {
  public boolean interrupted = false;
  public double counter = 0;

  public void run() {
    long before = System.currentTimeMillis();

    while (!interrupted) {
      counter += 0.001;
    }

    double tput = (counter * 1.0) / (System.currentTimeMillis() - before);
    System.out.println("Counter thread " + getName() +
      " incremented counter by " + tput + " per millisecond.");
  }
}
