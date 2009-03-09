/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.corpus;

import java.util.Random;

import javax.management.remote.JMXProviderException;

public class ExceptionBuilder {

  static Throwable build(Random r, double nestingProbability) {
    double rn = r.nextDouble();
    boolean nested = false;
    if (rn < nestingProbability) {
      nested = true;
    }

    Throwable cause = null;
    if(nested) {
      cause = makeThrowable(r, null);
    } 
    return makeThrowable(r, cause);
  }

  private static Throwable makeThrowable(Random r, Throwable cause) {
    int exType = r.nextInt(4);
    switch(exType) {
    case 0: return new IllegalArgumentException("an illegal argument was passed", cause);
    case 1: return new Exception("this is a test", cause);
    case 2: return new JMXProviderException("jmx provider exception error occured", cause);
    case 3: return new OutOfMemoryError("ran out of memory");
    }
    return null;
  }
  
}
