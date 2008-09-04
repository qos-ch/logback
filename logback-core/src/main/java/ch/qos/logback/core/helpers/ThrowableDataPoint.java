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

import java.io.Serializable;

/**
 * A container for either raw strings or StackTraceElementProxy instances.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class ThrowableDataPoint implements Serializable {

  private static final long serialVersionUID = -2891376879381358469L;

  enum ThrowableDataPointType {
    RAW, STEP;
  }

  String rawString;
  StackTraceElementProxy step;
  final ThrowableDataPointType type;

  public ThrowableDataPoint(String rawString) {
    this.rawString = rawString;
    this.type = ThrowableDataPointType.RAW;
  }

  public ThrowableDataPoint(StackTraceElement ste) {
    this.step = new StackTraceElementProxy(ste);
    this.type = ThrowableDataPointType.STEP;
  }

  public ThrowableDataPointType getType() {
    return type;
  }

  @Override
  public String toString() {
    switch (type) {
    case RAW:
      return rawString;
    case STEP:
      return step.getSTEAsString();
    }
    throw new IllegalStateException("Unreachable code");
  }

  @Override
  public int hashCode() {
    switch (type) {
    case RAW:
      return rawString.hashCode();
    case STEP:
      return step.hashCode();
    }
    throw new IllegalStateException("Unreachable code");
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ThrowableDataPoint other = (ThrowableDataPoint) obj;

    switch (type) {
    case RAW:
      if (rawString == null) {
        if (other.rawString != null)
          return false;
        else
          return true;
      } else {
        return rawString.equals(other.rawString);
      }
    case STEP:
      return step.equals(other.step);
    }
    throw new IllegalStateException("Unreachable code");
  }

}
