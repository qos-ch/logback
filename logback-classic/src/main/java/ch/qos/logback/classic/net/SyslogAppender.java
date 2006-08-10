/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.net;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.SyslogStartConverter;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.net.SyslogAppenderBase;

/**
 * 
 * @author Ceki G&uumllc&uuml;
 */
public class SyslogAppender extends SyslogAppenderBase {

  String prefixPattern;
  
  static final public String DEFAULT_SUFFIX_PATTERN = "[%thread] %logger %m%exception";
  
  public Layout buildLayout(String facilityStr) {
  
    PatternLayout pl = new PatternLayout();
    pl.getInstanceConverterMap().put("syslogStart", SyslogStartConverter.class.getName());
    
    if(prefixPattern == null) {
      prefixPattern = "%syslogStart{"+facilityStr+"}";
    }
    
    if(suffixPattern == null) {
      suffixPattern = DEFAULT_SUFFIX_PATTERN;
    }
    
    pl.setPattern(prefixPattern+suffixPattern);
    pl.setContext(getContext());
    pl.start();
    return pl;
  }
  
  /*
   * Convert a level to equivalent syslog severity. Only levels for printing methods
   * i.e DEBUG, WARN, INFO and ERROR are converted.
   * 
   * @see ch.qos.logback.core.net.SyslogAppenderBase#getSeverityForEvent(java.lang.Object)
   */
  @Override
  public int getSeverityForEvent(Object eventObject) {
    LoggingEvent event = (LoggingEvent) eventObject;
    return LevelToSyslogSeverity.convert(event);
  }

}
