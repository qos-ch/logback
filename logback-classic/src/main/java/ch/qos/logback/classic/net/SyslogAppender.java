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

import java.io.IOException;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.SyslogStartConverter;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.net.SyslogAppenderBase;
import ch.qos.logback.core.net.SyslogWriter;

/**
 * 
 * @author Ceki G&uumllc&uuml;
 */
public class SyslogAppender extends SyslogAppenderBase {

  String prefixPattern;
  PatternLayout prefixLayout;

  static final public String DEFAULT_SUFFIX_PATTERN = "[%thread] %logger %msg";

  public Layout buildLayout(String facilityStr) {

    prefixPattern = "%syslogStart{" + facilityStr + "}%nopex";
    prefixLayout = new PatternLayout();
    prefixLayout.getInstanceConverterMap().put("syslogStart",
        SyslogStartConverter.class.getName());
    prefixLayout.setPattern(prefixPattern);
    prefixLayout.setContext(getContext());
    prefixLayout.start();
    
    PatternLayout fullLayout = new PatternLayout();
    fullLayout.getInstanceConverterMap().put("syslogStart",
        SyslogStartConverter.class.getName());
    
    if (suffixPattern == null) {
      suffixPattern = DEFAULT_SUFFIX_PATTERN;
    }

    fullLayout.setPattern(prefixPattern + suffixPattern);
    fullLayout.setContext(getContext());
    fullLayout.start();
    return fullLayout;
  }

  /*
   * Convert a level to equivalent syslog severity. Only levels for printing
   * methods i.e DEBUG, WARN, INFO and ERROR are converted.
   * 
   * @see ch.qos.logback.core.net.SyslogAppenderBase#getSeverityForEvent(java.lang.Object)
   */
  @Override
  public int getSeverityForEvent(Object eventObject) {
    LoggingEvent event = (LoggingEvent) eventObject;
    return LevelToSyslogSeverity.convert(event);
  }

  @Override
  protected void postProcess(Object eventObject, SyslogWriter sw) {
    LoggingEvent event = (LoggingEvent) eventObject;
    
    String prefix = prefixLayout.doLayout(event);
    
    if (event.getThrowableInformation() != null) {
      String[] strRep = event.getThrowableInformation().getThrowableStrRep();
      try {
        for (String line : strRep) {
          sw.write(prefix + line);
          sw.flush();
        }
      } catch (IOException e) {
      }

    }

  }

}
