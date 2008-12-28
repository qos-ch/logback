/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.testUtil;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.LoggingEvent;

public class SampleConverter extends ClassicConverter {

  static public final String SAMPLE_STR = "sample";
  
  @Override
  public String convert(LoggingEvent event) {
    return SAMPLE_STR;
  }

}
