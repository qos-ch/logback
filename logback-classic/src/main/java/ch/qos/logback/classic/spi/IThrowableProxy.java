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

public interface IThrowableProxy {

  public String getMessage();
  public String getClassName();
  /**
   * The data point representation of the throwable proxy.
   */
  public ThrowableDataPoint[] getThrowableDataPointArray();
  public int getCommonFrames();
  public IThrowableProxy getCause();

}