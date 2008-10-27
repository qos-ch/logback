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

import ch.qos.logback.core.CoreGlobal;

public class ThrowableProxy implements java.io.Serializable {

  private static final long serialVersionUID = 6307784764626694851L;
  private ThrowableDataPoint[] tdpArray;
  private transient final Throwable throwable;
  private transient PackagingDataCalculator packagingDataCalculator;
  private boolean calculatedPackageData = false;
  
  public ThrowableProxy(Throwable throwable) {
    this.throwable = throwable;
    this.tdpArray = ThrowableToDataPointArray.convert(throwable);
  }

  public Throwable getThrowable() {
    return throwable;
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
    if(calculatedPackageData) {
      return;
    }
    PackagingDataCalculator pdc = this.getPackagingDataCalculator();
    if(pdc != null) {
      calculatedPackageData = true;
      pdc.calculate(tdpArray);
    }
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

  public void fullDump() {
    StringBuilder builder = new StringBuilder();
    for (ThrowableDataPoint tdp : getThrowableDataPointArray()) {
      String string = tdp.toString();
      builder.append(string);
      extraData(builder, tdp);
      builder.append(CoreGlobal.LINE_SEPARATOR);
    }
    System.out.println(builder.toString());
  }

  protected void extraData(StringBuilder builder, ThrowableDataPoint tdp) {
    StackTraceElementProxy step = tdp.getStackTraceElementProxy();
    if (step != null) {
      ClassPackagingData cpd = step.getClassPackagingData();
      if (cpd != null) {
        if(!cpd.isExact()){
          builder.append(" ~[")  ;
        } else {
          builder.append(" [")  ;
        }
        builder.append(cpd.getCodeLocation()).append(':').append(
            cpd.getVersion()).append(']');
      }
    }
  }
}
