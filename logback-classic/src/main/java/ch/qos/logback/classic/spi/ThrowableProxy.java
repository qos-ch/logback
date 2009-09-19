/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.core.CoreConstants;

public class ThrowableProxy implements IThrowableProxy {

  Throwable throwable;
  String className;
  String message;
  StackTraceElementProxy[] stackTraceElementProxyArray;
  int commonFrames;
  ThrowableProxy cause;

  private transient PackagingDataCalculator packagingDataCalculator;
  private boolean calculatedPackageData = false;

  public ThrowableProxy(Throwable throwable) {
   
    this.throwable = throwable;
    this.className = throwable.getClass().getName();
    this.message = throwable.getMessage();
    this.stackTraceElementProxyArray = ThrowableProxyUtil.steArrayToStepArray(throwable
        .getStackTrace());
    
    Throwable nested = throwable.getCause();
    
    if (nested != null) {
      this.cause = new ThrowableProxy(nested);
      this.cause.commonFrames = ThrowableProxyUtil
          .findNumberOfCommonFrames(nested.getStackTrace(),
              stackTraceElementProxyArray);
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

  public StackTraceElementProxy[] getStackTraceElementProxyArray() {
    return stackTraceElementProxyArray;
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
      pdc.calculate(this);
    }
  }



  public void fullDump() {
    StringBuilder builder = new StringBuilder();
    for (StackTraceElementProxy step : stackTraceElementProxyArray) {
      String string = step.toString();
      builder.append(CoreConstants.TAB).append(string);
      ThrowableProxyUtil.appendPackagingData(builder, step);
      builder.append(CoreConstants.LINE_SEPARATOR);
    }
    System.out.println(builder.toString());
  }


}
