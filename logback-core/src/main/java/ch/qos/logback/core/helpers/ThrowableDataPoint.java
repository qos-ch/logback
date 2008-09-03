/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.helpers;

public class ThrowableDataPoint {

  enum ThrowableDataPointType {
    RAW, STEP;
  }

  String rawString;
  StackTraceElementProxy step;
  final ThrowableDataPointType type;

  ThrowableDataPoint(String rawString) {
    this.rawString = rawString;
    this.type = ThrowableDataPointType.RAW;
  }

  ThrowableDataPoint(StackTraceElement ste) {
    this.step = new StackTraceElementProxy(ste);
    this.type = ThrowableDataPointType.STEP;
  }

  public ThrowableDataPointType getType() {
    return type;
  }
  
  @Override
  public String toString() {
    switch(type) {
    case RAW: return rawString;
    case STEP: return step.getSTEAsString();
    }
    throw new IllegalStateException("Unreachable code");
  }

}
