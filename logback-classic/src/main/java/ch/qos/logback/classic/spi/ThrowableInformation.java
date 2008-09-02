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

import ch.qos.logback.core.helpers.ThrowableToStringArray;

public class ThrowableInformation implements java.io.Serializable {

  private static final long serialVersionUID = 6307784764626694851L;
  private String[] sa;
  private transient final Throwable throwable;

  public ThrowableInformation(Throwable throwable) {
    this.throwable = throwable;
    sa = ThrowableToStringArray.extractStringRep(throwable, null);
  }

  public Throwable getThrowable() {
    return throwable;
  }
  
  /**
   * The string representation of the throwable  that this object
   * represents.
   */
  public String[] getThrowableStrRep() {
    return sa;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + Arrays.hashCode(sa);
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
    final ThrowableInformation other = (ThrowableInformation) obj;
    if (!Arrays.equals(sa, other.sa))
      return false;
    return true;
  }
  
  
}
