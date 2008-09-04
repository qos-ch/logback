/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2005, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.spi;

import java.util.Arrays;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.helpers.ThrowableDataPoint;
import ch.qos.logback.core.helpers.ThrowableToDataPointArray;

public class ThrowableProxy implements java.io.Serializable {

  private static final long serialVersionUID = 6307784764626694851L;
  private ThrowableDataPoint[] tdpArray;
  private transient final Throwable throwable;

  public ThrowableProxy(Throwable throwable) {
    this.throwable = throwable;
    tdpArray = ThrowableToDataPointArray.convert(throwable);
  }

  public Throwable getThrowable() {
    return throwable;
  }
  
  /**
   * The data point representation of the throwable proxy.
   */
  public ThrowableDataPoint[] getThrowableDataPointArray() {
    return tdpArray;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + Arrays.hashCode(tdpArray);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ThrowableProxy other = (ThrowableProxy) obj;
    if (!Arrays.equals(tdpArray, other.tdpArray))
      return false;
    return true;
  }
  
}
