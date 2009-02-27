/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.spi;

import java.util.Arrays;

import ch.qos.logback.core.CoreConstants;

public class ThrowableProxy implements IThrowableProxy {

  Throwable throwable;
  String className;
  String message;
  ThrowableDataPoint[] throwableDataPointArray;
  int commonFrames;
  ThrowableProxy cause;

  private transient PackagingDataCalculator packagingDataCalculator;
  private boolean calculatedPackageData = false;

  public ThrowableProxy(Throwable throwable) {
   
    this.throwable = throwable;
    this.className = throwable.getClass().getName();
    this.message = throwable.getMessage();
    this.throwableDataPointArray = ThrowableProxyUtil.stea2tdpa(throwable
        .getStackTrace());
    
    Throwable nested = throwable.getCause();
    
    if (nested != null) {
      this.cause = new ThrowableProxy(nested);
      this.cause.commonFrames = ThrowableProxyUtil
          .findNumberOfCommonFrames(nested.getStackTrace(),
              throwableDataPointArray);
    }
  }


  public Throwable getThrowable() {
    return throwable;
  }

  public String getMessage() {
    return message;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ch.qos.logback.classic.spi.IThrowableProxy#getClassName()
   */
  public String getClassName() {
    return className;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ch.qos.logback.classic.spi.IThrowableProxy#getThrowableDataPointArray()
   */
  public ThrowableDataPoint[] getThrowableDataPointArray() {
    return throwableDataPointArray;
  }

  public int getCommonFrames() {
    return commonFrames;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ch.qos.logback.classic.spi.IThrowableProxy#getCause()
   */
  public IThrowableProxy getCause() {
    return cause;
  }

  public PackagingDataCalculator getPackagingDataCalculator() {
    // if original instance (non-deserialized), and packagingDataCalculator
    // is not already initialized, then create an instance.
    // here we assume that (throwable == null) for deserialized instances
    if (throwable != null && packagingDataCalculator == null) {
      packagingDataCalculator = new PackagingDataCalculator();
    }
    return packagingDataCalculator;
  }

  public void calculatePackagingData() {
    if (calculatedPackageData) {
      return;
    }
    PackagingDataCalculator pdc = this.getPackagingDataCalculator();
    if (pdc != null) {
      calculatedPackageData = true;
      pdc.calculate(throwableDataPointArray);
    }
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + Arrays.hashCode(throwableDataPointArray);
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
    if (!Arrays.equals(throwableDataPointArray, other.throwableDataPointArray))
      return false;
    return true;
  }

  public void fullDump() {
    StringBuilder builder = new StringBuilder();
    for (ThrowableDataPoint tdp : getThrowableDataPointArray()) {
      String string = tdp.toString();
      builder.append(string);
      extraData(builder, tdp);
      builder.append(CoreConstants.LINE_SEPARATOR);
    }
    System.out.println(builder.toString());
  }

  protected void extraData(StringBuilder builder, ThrowableDataPoint tdp) {
    StackTraceElementProxy step = tdp.getStackTraceElementProxy();
    if (step != null) {
      ClassPackagingData cpd = step.getClassPackagingData();
      if (cpd != null) {
        if (!cpd.isExact()) {
          builder.append(" ~[");
        } else {
          builder.append(" [");
        }
        builder.append(cpd.getCodeLocation()).append(':').append(
            cpd.getVersion()).append(']');
      }
    }
  }
}
